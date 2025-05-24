package com.lin.tomato.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lin.tomato.navigation.Screen
import com.lin.tomato.ui.components.TimerDisplay
import com.lin.tomato.viewmodel.TimerViewModel

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    timerMode: String = Screen.Timer.WORK_MODE,
    viewModel: TimerViewModel = viewModel(
        factory = TimerViewModel.Factory,
        extras = MutableCreationExtras(
            with(LocalViewModelStoreOwner.current) {
                if (this is HasDefaultViewModelProviderFactory) {
                    this.defaultViewModelCreationExtras
                } else {
                    CreationExtras.Empty
                }
            }
        ).also {
            it[TimerViewModel.RUNNING_MODE] = timerMode
        }
    )
) {
    val timerState by viewModel.timerState.collectAsState()
    val workCycles by viewModel.workCycles.collectAsState()
    val breakCycles by viewModel.breakCycles.collectAsState()
    val isRunning by viewModel.runningState.collectAsState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        TimerDisplay(
            timerState = timerState,
            runningState = isRunning,
            workCycles = workCycles,
            breakCycles = breakCycles,
            onStartClick = viewModel::startTimer,
            onPauseClick = viewModel::pauseTimer,
            onResetClick = viewModel::resetTimer,
            onWorkDurationSelect = viewModel::setWorkDuration,
            onBreakDurationSelect = viewModel::setBreakDuration,
        )
    }
} 