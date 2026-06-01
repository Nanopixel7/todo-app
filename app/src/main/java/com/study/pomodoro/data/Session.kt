package com.study.pomodoro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Subject(val displayName: String) {
    CHINESE("CHINESE"),
    BOOK_READING("BOOK READING"),
    CODE_LEARNING("CODE LEARNING")
}

enum class TimerMode(val displayName: String, val durationMinutes: Int) {
    POMODORO("POMODORO", 25),
    SHORT_BREAK("SHORT BREAK", 5),
    LONG_BREAK("LONG BREAK", 15)
}

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val type: String,
    val durationMinutes: Int,
    val completedAt: Long,
    val wasCompleted: Boolean
)
