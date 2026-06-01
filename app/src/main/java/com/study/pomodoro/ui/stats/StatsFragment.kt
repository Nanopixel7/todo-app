package com.study.pomodoro.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.study.pomodoro.data.Subject
import com.study.pomodoro.data.SubjectMinutes
import com.study.pomodoro.databinding.FragmentStatsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()

    private val goalMinutes = 180

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        val dateFmt = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
        binding.tvDate.text = dateFmt.format(Date())

        viewModel.dailyMinutes.observe(viewLifecycleOwner) { list ->
            updateStats(list)
        }

        viewModel.weeklyMinutes.observe(viewLifecycleOwner) { list ->
            updateWeekly(list)
        }

        viewModel.dailyPomodoroCount.observe(viewLifecycleOwner) { count ->
            binding.tvPomodoroCount.text = "Today: $count pomodoros"
        }
    }

    private fun updateStats(list: List<SubjectMinutes>) {
        val map = list.associateBy { it.subject }

        fun bind(subject: Subject, progressBar: android.widget.ProgressBar, tvLabel: android.widget.TextView, tvProgress: android.widget.TextView) {
            val minutes = map[subject.name]?.totalMinutes ?: 0
            val pct = (minutes * 100 / goalMinutes).coerceAtMost(100)
            progressBar.progress = pct
            tvLabel.text = subject.displayName
            tvProgress.text = "${formatTime(minutes)} / 3h 00m"
        }

        bind(Subject.CHINESE, binding.progressChinese, binding.tvChineseLabel, binding.tvChineseProgress)
        bind(Subject.BOOK_READING, binding.progressReading, binding.tvReadingLabel, binding.tvReadingProgress)
        bind(Subject.CODE_LEARNING, binding.progressCoding, binding.tvCodingLabel, binding.tvCodingProgress)
    }

    private fun updateWeekly(list: List<SubjectMinutes>) {
        val map = list.associateBy { it.subject }
        binding.tvWeeklyChinese.text = "Chinese: ${formatTime(map[Subject.CHINESE.name]?.totalMinutes ?: 0)}"
        binding.tvWeeklyReading.text = "Reading: ${formatTime(map[Subject.BOOK_READING.name]?.totalMinutes ?: 0)}"
        binding.tvWeeklyCoding.text = "Coding: ${formatTime(map[Subject.CODE_LEARNING.name]?.totalMinutes ?: 0)}"
    }

    private fun formatTime(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
