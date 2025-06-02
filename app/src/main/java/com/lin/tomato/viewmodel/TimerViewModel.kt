package com.lin.tomato.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.lin.tomato.navigation.Screen
import com.lin.tomato.service.NotificationService
import com.lin.tomato.service.TimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TimerViewModel(private val application: Application, timerMode: String) : AndroidViewModel(application) {
    private val _timerState = MutableStateFlow<TimerState>(TimerState.Work())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private val _workCycles = MutableStateFlow(0)
    val workCycles: StateFlow<Int> = _workCycles.asStateFlow()

    private val _breakCycles = MutableStateFlow(0)
    val breakCycles: StateFlow<Int> = _breakCycles.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val runningState: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var timerJob: Job? = null
    private val notificationService = NotificationService(application)

    private val dryRun: Boolean = timerMode == Screen.Timer.DRY_RUN_MODE
    private val secondInMinutes = if (dryRun) 1 else 60

    init {
        if (dryRun) {
            setWorkDuration(5)
        }
    }
    fun startTimer() {
        if (_isRunning.value) return
        _isRunning.value = true
        startTimerService(application, _timerState.value.remainingSeconds)
        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0) {
                delay(1000)
                _timerState.value = when (val currentState = _timerState.value) {
                    is TimerState.Work -> currentState.copy(remainingSeconds = currentState.remainingSeconds - 1)
                    is TimerState.Break -> currentState.copy(remainingSeconds = currentState.remainingSeconds - 1)
                }
            }
            // When timer finishes, show notification and switch mode
            showTimerCompleteNotification()
            // Increment cycle counter before switching
            when (_timerState.value) {
                is TimerState.Work -> _workCycles.value++
                is TimerState.Break -> _breakCycles.value++
            }
            switchTimer()
            _isRunning.value = false
            stopTimerService(application)
        }
    }

    fun startTimerService(context: Context, durationSeconds: Int) {
        val intent = Intent(context, TimerService::class.java).apply {
            putExtra(TimerService.EXTRA_DURATION, durationSeconds)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTimerService(context: Context) {
        val intent = Intent(context, TimerService::class.java)
        context.stopService(intent)
    }

    private fun showTimerCompleteNotification() {
        val (title, message) = when (_timerState.value) {
            is TimerState.Work -> "Work Time Complete!" to "Time for a break!"
            is TimerState.Break -> "Break Time Complete!" to "Ready to get back to work?"
        }
        notificationService.showTimerCompleteNotification(title, message)
    }

    fun pauseTimer() {
        stopTimerService(application)
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun resetTimer() {
        stopTimerService(application)
        timerJob?.cancel()
        _isRunning.value = false
        _timerState.value = when (_timerState.value) {
            is TimerState.Work -> TimerState.Work()
            is TimerState.Break -> TimerState.Break()
        }
    }

    fun resetCycles() {
        _workCycles.value = 0
        _breakCycles.value = 0
    }

    fun setWorkDuration(minutes: Int) {
        if (_timerState.value is TimerState.Work && !_isRunning.value) {
            _timerState.value = TimerState.Work(minutes * secondInMinutes)
        }
    }

    fun setBreakDuration(minutes: Int) {
        if (_timerState.value is TimerState.Break && !_isRunning.value) {
            _timerState.value = TimerState.Break(minutes * secondInMinutes)
        }
    }

    private fun switchTimer() {
        _timerState.value = when (_timerState.value) {
            is TimerState.Work -> TimerState.Break()
            is TimerState.Break -> TimerState.Work()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        val RUNNING_MODE = object : CreationExtras.Key<String> {}
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                val mode = this[RUNNING_MODE] ?: Screen.Timer.WORK_MODE
                TimerViewModel(application, mode)
            }
        }
    }
}

sealed class TimerState(open val remainingSeconds: Int) {
    data class Work(override val remainingSeconds: Int = 45 * 60) : TimerState(remainingSeconds)
    data class Break(override val remainingSeconds: Int = 10 * 60) : TimerState(remainingSeconds)
}
