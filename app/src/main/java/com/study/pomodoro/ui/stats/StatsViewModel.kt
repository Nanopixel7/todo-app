package com.study.pomodoro.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.study.pomodoro.data.AppDatabase
import com.study.pomodoro.data.SubjectMinutes
import java.util.Calendar

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).sessionDao()

    private fun todayRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return Pair(start, cal.timeInMillis)
    }

    private fun weekStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    val dailyMinutes: LiveData<List<SubjectMinutes>> by lazy {
        val (start, end) = todayRange()
        dao.getDailyMinutesPerSubject(start, end)
    }

    val weeklyMinutes: LiveData<List<SubjectMinutes>> by lazy {
        dao.getWeeklyMinutesPerSubject(weekStart())
    }

    val dailyPomodoroCount: LiveData<Int> by lazy {
        val (start, end) = todayRange()
        dao.getDailyPomodoroCount(start, end)
    }
}
