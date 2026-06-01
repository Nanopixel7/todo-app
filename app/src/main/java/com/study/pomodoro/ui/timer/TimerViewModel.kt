package com.study.pomodoro.ui.timer

import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.study.pomodoro.data.AppDatabase
import com.study.pomodoro.data.Session
import com.study.pomodoro.data.Subject
import com.study.pomodoro.data.TimerMode
import kotlinx.coroutines.launch

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).sessionDao()

    private val _timerMode = MutableLiveData(TimerMode.POMODORO)
    val timerMode: LiveData<TimerMode> = _timerMode

    private val _currentSubject = MutableLiveData(Subject.CHINESE)
    val currentSubject: LiveData<Subject> = _currentSubject

    private val _timeLeftMs = MutableLiveData<Long>()
    val timeLeftMs: LiveData<Long> = _timeLeftMs

    private val _isRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = _isRunning

    private val _pomodoroCount = MutableLiveData(0)
    val pomodoroCount: LiveData<Int> = _pomodoroCount

    private val _sessionCompleted = MutableLiveData(false)
    val sessionCompleted: LiveData<Boolean> = _sessionCompleted

    private var countDownTimer: CountDownTimer? = null
    private var sessionStartMs: Long = 0L
    private var totalDurationMs: Long = 0L

    init {
        resetTimeToMode()
    }

    private fun resetTimeToMode() {
        val mode = _timerMode.value ?: TimerMode.POMODORO
        totalDurationMs = mode.durationMinutes * 60_000L
        _timeLeftMs.value = totalDurationMs
    }

    fun startTimer() {
        if (_isRunning.value == true) return
        val timeLeft = _timeLeftMs.value ?: totalDurationMs
        sessionStartMs = System.currentTimeMillis()
        _isRunning.value = true

        countDownTimer = object : CountDownTimer(timeLeft, 500L) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftMs.value = millisUntilFinished
            }

            override fun onFinish() {
                _timeLeftMs.value = 0L
                _isRunning.value = false
                onTimerFinished(wasCompleted = true)
            }
        }.start()
    }

    fun pauseTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        countDownTimer?.cancel()
        _isRunning.value = false
        resetTimeToMode()
        _sessionCompleted.value = false
    }

    fun cycleSubject() {
        if (_isRunning.value == true) return
        val subjects = Subject.values()
        val current = _currentSubject.value ?: Subject.CHINESE
        val next = subjects[(current.ordinal + 1) % subjects.size]
        _currentSubject.value = next
    }

    fun cycleMode() {
        if (_isRunning.value == true) return
        val modes = TimerMode.values()
        val current = _timerMode.value ?: TimerMode.POMODORO
        val next = modes[(current.ordinal + 1) % modes.size]
        _timerMode.value = next
        resetTimeToMode()
        _sessionCompleted.value = false
    }

    private fun onTimerFinished(wasCompleted: Boolean) {
        val mode = _timerMode.value ?: return
        val subject = _currentSubject.value ?: return

        viewModelScope.launch {
            dao.insertSession(
                Session(
                    subject = subject.name,
                    type = mode.name,
                    durationMinutes = mode.durationMinutes,
                    completedAt = System.currentTimeMillis(),
                    wasCompleted = wasCompleted
                )
            )
        }

        if (mode == TimerMode.POMODORO) {
            val count = (_pomodoroCount.value ?: 0) + 1
            _pomodoroCount.value = count % 4
        }

        _sessionCompleted.value = true
        autoAdvanceMode()
    }

    private fun autoAdvanceMode() {
        val mode = _timerMode.value ?: return
        val pomCount = _pomodoroCount.value ?: 0
        _timerMode.value = when (mode) {
            TimerMode.POMODORO -> if (pomCount == 0) TimerMode.LONG_BREAK else TimerMode.SHORT_BREAK
            TimerMode.SHORT_BREAK, TimerMode.LONG_BREAK -> TimerMode.POMODORO
        }
        resetTimeToMode()
    }

    fun getProgress(): Float {
        val left = _timeLeftMs.value ?: totalDurationMs
        return if (totalDurationMs == 0L) 0f else 1f - (left.toFloat() / totalDurationMs.toFloat())
    }

    override fun onCleared() {
        countDownTimer?.cancel()
        val timeLeft = _timeLeftMs.value ?: 0L
        if (_isRunning.value == true && totalDurationMs > 0L && timeLeft < totalDurationMs) {
            val elapsed = ((totalDurationMs - timeLeft) / 60_000L).toInt()
            if (elapsed > 0) {
                val mode = _timerMode.value ?: return
                val subject = _currentSubject.value ?: return
                viewModelScope.launch {
                    dao.insertSession(
                        Session(
                            subject = subject.name,
                            type = mode.name,
                            durationMinutes = elapsed,
                            completedAt = System.currentTimeMillis(),
                            wasCompleted = false
                        )
                    )
                }
            }
        }
        super.onCleared()
    }
}
