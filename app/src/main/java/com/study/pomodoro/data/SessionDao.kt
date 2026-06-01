package com.study.pomodoro.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: Session)

    @Query("SELECT * FROM sessions ORDER BY completedAt DESC")
    fun getAllSessions(): LiveData<List<Session>>

    @Query("SELECT * FROM sessions WHERE completedAt >= :startMs AND completedAt < :endMs ORDER BY completedAt DESC")
    fun getSessionsForDate(startMs: Long, endMs: Long): LiveData<List<Session>>

    @Query("""
        SELECT subject, SUM(durationMinutes) as totalMinutes
        FROM sessions
        WHERE completedAt >= :startMs AND completedAt < :endMs AND type = 'POMODORO' AND wasCompleted = 1
        GROUP BY subject
    """)
    fun getDailyMinutesPerSubject(startMs: Long, endMs: Long): LiveData<List<SubjectMinutes>>

    @Query("""
        SELECT subject, SUM(durationMinutes) as totalMinutes
        FROM sessions
        WHERE completedAt >= :startMs AND type = 'POMODORO' AND wasCompleted = 1
        GROUP BY subject
    """)
    fun getWeeklyMinutesPerSubject(startMs: Long): LiveData<List<SubjectMinutes>>

    @Query("SELECT COUNT(*) FROM sessions WHERE completedAt >= :startMs AND completedAt < :endMs AND type = 'POMODORO' AND wasCompleted = 1")
    fun getDailyPomodoroCount(startMs: Long, endMs: Long): LiveData<Int>
}

data class SubjectMinutes(
    val subject: String,
    val totalMinutes: Int
)
