package com.study.pomodoro.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.study.pomodoro.data.AppDatabase

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).sessionDao()
    val sessions = dao.getAllSessions()
}
