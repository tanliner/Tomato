package com.lin.tomato.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lin.tomato.ui.theme.Warning
import com.lin.tomato.viewmodel.TimerState

@Composable
fun TimerDisplay(
    timerState: TimerState,
    runningState: Boolean,
    workCycles: Int,
    breakCycles: Int,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit,
    onWorkDurationSelect: (Int) -> Unit,
    onBreakDurationSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer type indicator
        Text(
            text = when (timerState) {
                is TimerState.Work -> "Work Time"
                is TimerState.Break -> "Break Time"
            },
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Timer display
        Text(
            text = formatTime(timerState.remainingSeconds),
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Duration selection
        when (timerState) {
            is TimerState.Work -> WorkDurationSelector(isRunning = runningState, onWorkDurationSelect)
            is TimerState.Break -> BreakDurationSelector(isRunning = runningState, onBreakDurationSelect)
        }

        Spacer(modifier = Modifier.height(24.dp))

        MainControlButton(runningState, onStartClick, onPauseClick, onResetClick)

        Spacer(modifier = Modifier.weight(1f))

        WorkingCycles(workCycles, breakCycles)
    }
}

@Composable
private fun WorkingCycles(workCycles: Int, breakCycles: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Work Cycles",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = workCycles.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Break Cycles",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = breakCycles.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun MainControlButton(
    runningState: Boolean,
    onStartClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(onClick = onResetClick) {
            Text(
                "Reset",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
                color = Warning
            )
        }
        Button(onClick = if (runningState) onPauseClick else onStartClick) {
            Text(
                if (runningState) "Pause" else "Start",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall
            )
        }
    }
}

@Composable
private fun WorkDurationSelector(isRunning: Boolean, onDurationSelect: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select Work Duration",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationButton(duration = 25, isRunning = isRunning, onClick = onDurationSelect)
            DurationButton(duration = 30, isRunning = isRunning, onClick = onDurationSelect)
            DurationButton(duration = 45, isRunning = isRunning, onClick = onDurationSelect)
        }
    }
}

@Composable
private fun BreakDurationSelector(isRunning: Boolean, onDurationSelect: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select Break Duration",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationButton(duration = 5, isRunning = isRunning, onClick = onDurationSelect)
            DurationButton(duration = 10, isRunning = isRunning, onClick = onDurationSelect)
            DurationButton(duration = 15, isRunning = isRunning, onClick = onDurationSelect)
        }
    }
}

@Composable
private fun DurationButton(duration: Int, isRunning: Boolean, onClick: (Int) -> Unit) {
    OutlinedButton(
        enabled = !isRunning,
        onClick = { onClick(duration) },
        modifier = Modifier.width(72.dp)
    ) {
        Text("${duration}m")
    }
}

@Preview
@Composable
fun TimerDisplayWork_Preview() {
    TimerDisplay(
        TimerState.Work(2 * 60), false, 0, 0, {}, {}, {}, {}, {},
    )
}

@Preview
@Composable
fun TimerDisplayBreak_Preview() {
    TimerDisplay(
        TimerState.Break(60), false, 0, 0, {}, {}, {}, {}, {},
    )
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%02d:%02d".format(minutes, remainingSeconds)
}
