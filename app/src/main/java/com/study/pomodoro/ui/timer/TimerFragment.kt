package com.study.pomodoro.ui.timer

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.study.pomodoro.R
import com.study.pomodoro.data.TimerMode
import com.study.pomodoro.databinding.FragmentTimerBinding
import com.study.pomodoro.service.TimerService

class TimerFragment : Fragment() {

    private var _binding: FragmentTimerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dots = listOf(binding.dot1, binding.dot2, binding.dot3, binding.dot4)

        viewModel.timeLeftMs.observe(viewLifecycleOwner) { ms ->
            val totalSec = (ms / 1000L).toInt()
            val min = totalSec / 60
            val sec = totalSec % 60
            binding.tvTime.text = String.format("%02d:%02d", min, sec)
            binding.circularTimer.progress = viewModel.getProgress()
            updateNotification()
        }

        viewModel.isRunning.observe(viewLifecycleOwner) { running ->
            binding.circularTimer.isPlaying = running
            binding.circularTimer.showIcon = !running
            binding.tvTime.visibility = if (running) View.VISIBLE else View.GONE
        }

        viewModel.currentSubject.observe(viewLifecycleOwner) { subject ->
            updateSubjectLabel()
        }

        viewModel.timerMode.observe(viewLifecycleOwner) { _ ->
            updateSubjectLabel()
        }

        viewModel.pomodoroCount.observe(viewLifecycleOwner) { count ->
            dots.forEachIndexed { index, dot ->
                dot.setImageResource(
                    if (index < count) R.drawable.dot_filled else R.drawable.dot_empty
                )
            }
        }

        viewModel.sessionCompleted.observe(viewLifecycleOwner) { completed ->
            if (completed) vibrate()
        }

        binding.circularTimer.setOnClickListener {
            if (viewModel.isRunning.value == true) viewModel.pauseTimer()
            else viewModel.startTimer()
        }

        binding.circularTimer.setOnLongClickListener {
            viewModel.resetTimer()
            true
        }

        binding.btnSubjectUp.setOnClickListener { viewModel.cycleSubject() }
        binding.btnSubjectDown.setOnClickListener { viewModel.cycleSubject() }
        binding.tvSubjectMode.setOnClickListener { viewModel.cycleMode() }
        binding.tvSubjectMode.setOnLongClickListener { viewModel.cycleMode(); true }

        binding.btnMenu.setOnClickListener { showMenu(it) }
    }

    private fun updateSubjectLabel() {
        val subject = viewModel.currentSubject.value ?: return
        val mode = viewModel.timerMode.value ?: return
        binding.tvSubjectMode.text = "${mode.displayName}  ${mode.durationMinutes} MIN"
        binding.tvSubject.text = subject.displayName
    }

    private fun showMenu(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menu.add(0, 1, 0, "History")
        popup.menu.add(0, 2, 1, "Stats")
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> findNavController().navigate(R.id.action_timer_to_history)
                2 -> findNavController().navigate(R.id.action_timer_to_stats)
            }
            true
        }
        popup.show()
    }

    private fun vibrate() {
        val vibrator = ContextCompat.getSystemService(requireContext(), Vibrator::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
        }
    }

    private fun updateNotification() {
        val ms = viewModel.timeLeftMs.value ?: return
        if (viewModel.isRunning.value != true) return
        val totalSec = (ms / 1000L).toInt()
        val time = String.format("%02d:%02d", totalSec / 60, totalSec % 60)
        val subject = viewModel.currentSubject.value?.displayName ?: return
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            putExtra(TimerService.EXTRA_SUBJECT, subject)
            putExtra(TimerService.EXTRA_TIME, time)
        }
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    override fun onStop() {
        super.onStop()
        if (viewModel.isRunning.value == true) updateNotification()
        else requireContext().stopService(Intent(requireContext(), TimerService::class.java))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
