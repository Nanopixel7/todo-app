package com.study.pomodoro.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.study.pomodoro.R
import com.study.pomodoro.data.Session
import com.study.pomodoro.data.Subject
import com.study.pomodoro.data.TimerMode
import com.study.pomodoro.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessionAdapter : ListAdapter<Session, SessionAdapter.VH>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Session>() {
            override fun areItemsTheSame(a: Session, b: Session) = a.id == b.id
            override fun areContentsTheSame(a: Session, b: Session) = a == b
        }
        private val DATE_FMT = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    }

    inner class VH(private val b: ItemSessionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(session: Session) {
            val subjectEnum = Subject.values().firstOrNull { it.name == session.subject }
            val modeEnum = TimerMode.values().firstOrNull { it.name == session.type }

            b.tvSubject.text = subjectEnum?.displayName ?: session.subject
            b.tvMode.text = modeEnum?.displayName ?: session.type
            b.tvDate.text = DATE_FMT.format(Date(session.completedAt))
            b.tvDuration.text = "${session.durationMinutes}m"
            b.tvStatus.text = if (session.wasCompleted) "✓" else "✗"
            b.tvStatus.setTextColor(
                itemView.context.getColor(if (session.wasCompleted) R.color.coding_accent else R.color.dot_inactive)
            )

            val dotColor = when (subjectEnum) {
                Subject.CHINESE -> R.color.chinese_accent
                Subject.BOOK_READING -> R.color.reading_accent
                Subject.CODE_LEARNING -> R.color.coding_accent
                null -> R.color.white
            }
            b.viewDot.setBackgroundColor(itemView.context.getColor(dotColor))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))
}
