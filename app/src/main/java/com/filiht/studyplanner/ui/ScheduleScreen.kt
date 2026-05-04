package com.filiht.studyplanner.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.filiht.studyplanner.model.StudyTask
import com.filiht.studyplanner.model.Subject
import com.filiht.studyplanner.notification.StudyAlarmScheduler
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme

@Composable
fun ScheduleScreen(
    viewModel: StudyViewModel,
    onTaskClick: (StudyTask) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    val alarmScheduler = remember { StudyAlarmScheduler(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    ScheduleScreenContent(
        tasks = tasks,
        subjects = subjects,
        selectedDay = selectedDay,
        onTaskClick = onTaskClick,
        onSettingsClick = onSettingsClick,
        onDaySelected = { viewModel.selectDay(it) },
        onDeleteTask = { taskId ->
            alarmScheduler.cancel(taskId)
            viewModel.removeTask(taskId)
        },
        onCompleteTask = { viewModel.completeTask(it) },
        onAddTask = { subjectId, topic, time ->
            val subjectName = subjects.find { it.id == subjectId }?.name ?: ""
            viewModel.addTask(
                StudyTask(
                    subjectId = subjectId,
                    subjectName = subjectName,
                    topic = topic,
                    time = time,
                    day = selectedDay,
                    isCompleted = false
                )
            ) { savedTask ->
                alarmScheduler.schedule(savedTask)
            }
        },
        onAddSubject = { viewModel.addSubject(it) },
        onDeleteSubject = { viewModel.deleteSubject(it) }
    )
}

@Composable
fun ScheduleScreenContent(
    tasks: List<StudyTask>,
    subjects: List<Subject>,
    selectedDay: String,
    onTaskClick: (StudyTask) -> Unit,
    onSettingsClick: () -> Unit,
    onDaySelected: (String) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onCompleteTask: (StudyTask) -> Unit,
    onAddTask: (Int, String, String) -> Unit,
    onAddSubject: (String) -> Unit,
    onDeleteSubject: (Int) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Study Planner",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            DaySelector(
                selectedDay = selectedDay,
                onDaySelected = onDaySelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(tasks) { task ->
                    StyledTaskCard(
                        task = task,
                        onTaskClick = { onTaskClick(task) },
                        onDelete = { onDeleteTask(task.id) },
                        onToggleComplete = { onCompleteTask(task) }
                    )
                }
                item {
                    AddSessionButton(onClick = { showAddDialog = true })
                }
            }
        }
    }

    if (showAddDialog) {
        NewStudySessionModal(
            subjects = subjects,
            onDismiss = { showAddDialog = false },
            onAdd = { subjectId, topic, time ->
                onAddTask(subjectId, topic, time)
                showAddDialog = false
            },
            onAddSubject = onAddSubject,
            onDeleteSubject = onDeleteSubject
        )
    }
}

@Composable
fun StyledTaskCard(
    task: StudyTask,
    onTaskClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val contentAlpha = if (task.isCompleted) 0.5f else 1f
    
    val completeInteractionSource = remember { MutableInteractionSource() }
    val deleteInteractionSource = remember { MutableInteractionSource() }
    
    val isCompletePressed by completeInteractionSource.collectIsPressedAsState()
    val isDeletePressed by deleteInteractionSource.collectIsPressedAsState()

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(enabled = !task.isCompleted) { onTaskClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha)
            ) {
                Icon(
                    Icons.Outlined.Schedule, 
                    null, 
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, 
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    task.time, 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(1.dp).height(48.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)))
            Spacer(modifier = Modifier.width(16.dp))

            // Info Section
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Text(
                        task.subjectName.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Text(
                    text = task.topic, 
                    style = MaterialTheme.typography.titleLarge.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 4.dp).alpha(contentAlpha)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Icon(
                        imageVector = if(task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if(task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if(task.isCompleted) " Completed" else " Pending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Actions Section
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Complete Button
                Surface(
                    onClick = onToggleComplete,
                    interactionSource = completeInteractionSource,
                    shape = CircleShape,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = if (task.isCompleted) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Delete Button
                Surface(
                    onClick = onDelete,
                    interactionSource = deleteInteractionSource,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DaySelector(selectedDay: String, onDaySelected: (String) -> Unit) {
    val days = listOf(
        "Monday" to "Mon",
        "Tuesday" to "Tue",
        "Wednesday" to "Wed",
        "Thursday" to "Thu",
        "Friday" to "Fri",
        "Saturday" to "Sat",
        "Sunday" to "Sun"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (fullName, initial) ->
            val isSelected = selectedDay == fullName
            Surface(
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(45.dp)
                    .clickable { onDaySelected(fullName) }
                    .then(if (!isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(16.dp)) else Modifier)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddSessionButton(onClick: () -> Unit) {
    val strokeColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(100.dp)
            .drawBehind {
                drawRoundRect(
                    color = strokeColor,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("ADD TOPIC", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    val sampleSubjects = listOf(
        Subject(1, "Maths"),
        Subject(2, "Science"),
        Subject(3, "History")
    )
    val sampleTasks = listOf(
        StudyTask(1, 1, "Maths", "Calculus Basics", "09:00 AM", "Monday", false),
        StudyTask(2, 2, "Science", "Physics: Quantum Mechanics", "11:00 AM", "Monday", true),
        StudyTask(3, 3, "History", "The French Revolution", "02:00 PM", "Monday", false)
    )

    StudyPlannerTheme {
        ScheduleScreenContent(
            tasks = sampleTasks,
            subjects = sampleSubjects,
            selectedDay = "Monday",
            onTaskClick = {},
            onSettingsClick = {},
            onDaySelected = {},
            onDeleteTask = {},
            onCompleteTask = {},
            onAddTask = { _, _, _ -> },
            onAddSubject = {},
            onDeleteSubject = {}
        )
    }
}
