package com.filiht.studyplanner.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filiht.studyplanner.model.StudyTask
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme

@Composable
fun TimerScreen(
    task: StudyTask?,
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    TimerScreenContent(
        task = task,
        timeLeft = viewModel.timeLeft.value,
        totalTime = viewModel.totalTime.value,
        isTimerRunning = viewModel.isTimerRunning.value,
        isFocusMode = viewModel.isFocusMode.value,
        focusDuration = viewModel.focusDurationMinutes.value,
        breakDuration = viewModel.breakDurationMinutes.value,
        formatTime = { viewModel.formatTime(it) },
        onBack = onBack,
        onReset = { viewModel.resetTimer() },
        onToggleTimer = { if (viewModel.isTimerRunning.value) viewModel.pauseTimer() else viewModel.startTimer(task) },
        onFinish = { task?.let { viewModel.completeTask(it) }; onBack() },
        onToggleMode = { viewModel.toggleMode(it) },
        onSaveSettings = { f, b -> viewModel.setTimerSettings(f, b) }
    )
}

@Composable
fun TimerScreenContent(
    task: StudyTask?,
    timeLeft: Long,
    totalTime: Long,
    isTimerRunning: Boolean,
    isFocusMode: Boolean,
    focusDuration: Long,
    breakDuration: Long,
    formatTime: (Long) -> String,
    onBack: () -> Unit,
    onReset: () -> Unit,
    onToggleTimer: () -> Unit,
    onFinish: () -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onSaveSettings: (Long, Long) -> Unit
) {
    var showSettings by remember { mutableStateOf(false) }
    val primaryColor = if (isFocusMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val badgeBackgroundColor = primaryColor.copy(alpha = 0.15f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .border(0.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
            }

            // Mode Switcher
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(4.dp)
                ) {
                    ModeButton(
                        text = "FOCUS",
                        isSelected = isFocusMode,
                        activeColor = MaterialTheme.colorScheme.primary,
                        onClick = { onToggleMode(true) }
                    )
                    ModeButton(
                        text = "BREAK",
                        isSelected = !isFocusMode,
                        activeColor = MaterialTheme.colorScheme.secondary,
                        onClick = { onToggleMode(false) }
                    )
                }
            }

            IconButton(
                onClick = { showSettings = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Badge
        Surface(
            color = badgeBackgroundColor,
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isFocusMode) Icons.Default.Psychology else Icons.Default.Coffee,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isFocusMode) "DEEP WORK" else "TIME TO REST",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (isFocusMode) (task?.topic ?: "Focus Session") else "Short Break",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.weight(1f))

        // Timer Circle
        Box(
            modifier = Modifier.size(320.dp),
            contentAlignment = Alignment.Center
        ) {
            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
            
            if (isTimerRunning) {
                Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.05f),
                        radius = size.minDimension / 2 + 10.dp.toPx()
                    )
                }
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                drawCircle(
                    color = outlineVariant.copy(alpha = 0.5f),
                    style = Stroke(width = strokeWidth)
                )
                
                drawArc(
                    color = primaryColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Text(
                text = formatTime(timeLeft),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Controls
        Row(
            modifier = Modifier.padding(bottom = 64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(60.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(28.dp))
            }

            FilledIconButton(
                onClick = onToggleTimer,
                modifier = Modifier.size(92.dp),
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor)
            ) {
                Icon(
                    imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
            }

            IconButton(
                onClick = onFinish,
                modifier = Modifier
                    .size(60.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Check, 
                    contentDescription = "Complete Session", 
                    tint = if (task?.isCompleted == true) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurface, 
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    if (showSettings) {
        TimerSettingsDialog(
            initialFocus = focusDuration,
            initialBreak = breakDuration,
            onDismiss = { showSettings = false },
            onSave = { f, b ->
                onSaveSettings(f, b)
                showSettings = false
            }
        )
    }
}

@Composable
fun ModeButton(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(90.dp)
            .background(
                if (isSelected) MaterialTheme.colorScheme.background else Color.Transparent,
                RoundedCornerShape(11.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSettingsDialog(
    initialFocus: Long,
    initialBreak: Long,
    onDismiss: () -> Unit,
    onSave: (Long, Long) -> Unit
) {
    var focusValue by remember { mutableStateOf(initialFocus.toFloat()) }
    var breakValue by remember { mutableStateOf(initialBreak.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Timer Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.background(MaterialTheme.colorScheme.outlineVariant, CircleShape).size(36.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("FOCUS DURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${focusValue.toInt()}m", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = focusValue,
                        onValueChange = { focusValue = it },
                        valueRange = 1f..60f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("BREAK DURATION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${breakValue.toInt()}m", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                    }
                    Slider(
                        value = breakValue,
                        onValueChange = { breakValue = it },
                        valueRange = 1f..30f,
                        colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.secondary, activeTrackColor = MaterialTheme.colorScheme.secondary, inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { onSave(focusValue.toLong(), breakValue.toLong()) },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    val sampleTask = StudyTask(
        id = 1,
        subjectId = 1,
        subjectName = "Mathematics",
        topic = "Calculus Basics",
        time = "09:00 AM",
        day = "Monday",
        isCompleted = false
    )
    StudyPlannerTheme {
        TimerScreenContent(
            task = sampleTask,
            timeLeft = 1500L,
            totalTime = 1500L,
            isTimerRunning = true,
            isFocusMode = true,
            focusDuration = 25L,
            breakDuration = 5L,
            formatTime = { seconds ->
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                "%02d:%02d".format(minutes, remainingSeconds)
            },
            onBack = {},
            onReset = {},
            onToggleTimer = {},
            onFinish = {},
            onToggleMode = {},
            onSaveSettings = { _, _ -> }
        )
    }
}
