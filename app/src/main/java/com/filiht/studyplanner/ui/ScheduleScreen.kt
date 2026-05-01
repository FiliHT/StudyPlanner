package com.filiht.studyplanner.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filiht.studyplanner.model.StudyTask
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: StudyViewModel,
    onTaskClick: (StudyTask) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()

    ScheduleScreenContent(
        tasks = tasks,
        selectedDay = selectedDay,
        onTaskClick = onTaskClick,
        onDaySelected = { viewModel.selectDay(it) },
        onDeleteTask = { viewModel.removeTask(it) },
        onCompleteTask = { viewModel.completeTask(it) },
        onAddTask = { subject, topic, time ->
            viewModel.addTask(
                StudyTask(
                    subject = subject,
                    topic = topic,
                    time = time,
                    day = selectedDay,
                    isCompleted = false
                )
            )
        }
    )
}

@Composable
fun ScheduleScreenContent(
    tasks: List<StudyTask>,
    selectedDay: String,
    onTaskClick: (StudyTask) -> Unit,
    onDaySelected: (String) -> Unit,
    onDeleteTask: (Int) -> Unit,
    onCompleteTask: (StudyTask) -> Unit,
    onAddTask: (String, String, String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF5D5FEF))
                    .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Study Planner",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
//
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FE))
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
            onDismiss = { showAddDialog = false },
            onAdd = { subject, topic, time ->
                onAddTask(subject, topic, time)
                showAddDialog = false
            }
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
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White)
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
                Icon(Icons.Outlined.Schedule, null, tint = Color(0xFF9EA3AE), modifier = Modifier.size(24.dp))
                Text(
                    task.time, 
                    style = MaterialTheme.typography.labelLarge, 
                    color = Color(0xFF9EA3AE), 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))
            Box(modifier = Modifier.width(1.dp).height(48.dp).background(Color(0xFFF0F0F0)))
            Spacer(modifier = Modifier.width(16.dp))

            // Info Section
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = Color(0xFFF0F2FF), 
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Text(
                        task.subject.uppercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = Color(0xFF5D5FEF),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Text(
                    text = task.topic, 
                    style = MaterialTheme.typography.titleLarge.copy(
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ), 
                    fontWeight = FontWeight.ExtraBold, 
                    color = if (task.isCompleted) Color.Gray else Color(0xFF1A1C1E),
                    modifier = Modifier.padding(vertical = 4.dp).alpha(contentAlpha)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.alpha(contentAlpha)
                ) {
                    Icon(
                        imageVector = if(task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if(task.isCompleted) Color(0xFF00A389) else Color(0xFF9EA3AE),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if(task.isCompleted) " Completed" else " Pending",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if(task.isCompleted) Color(0xFF00A389) else Color(0xFF9EA3AE),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Actions Section - Vertical Column as per image
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Complete Button
                Surface(
                    onClick = onToggleComplete,
                    interactionSource = completeInteractionSource,
                    shape = CircleShape,
                    color = if (isCompletePressed || task.isCompleted) Color(0xFFE6F6F3) else Color(0xFFF5F7FA),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Complete",
                            tint = if (task.isCompleted) Color(0xFF00A389) else Color(0xFF9EA3AE),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Delete Button
                Surface(
                    onClick = onDelete,
                    interactionSource = deleteInteractionSource,
                    shape = CircleShape,
                    color = if (isDeletePressed) Color(0xFFFFEBEE) else Color(0xFFF5F7FA),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Delete",
                            tint = if (isDeletePressed) Color.Red else Color(0xFF9EA3AE),
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
                color = if (isSelected) Color(0xFF5D5FEF) else Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(45.dp)
                    .clickable { onDaySelected(fullName) }
                    .then(if (!isSelected) Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(16.dp)) else Modifier)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = initial,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AddSessionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(100.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray,
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)),
                    cornerRadius = CornerRadius(24.dp.toPx())
                )
            }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.LightGray)
            Text("ADD SESSION", color = Color.LightGray, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleScreenPreview() {
    val sampleTasks = listOf(
        StudyTask(1, "Maths", "Calculus Basics", "09:00 AM", "Monday", false),
        StudyTask(2, "Science", "Physics - Forces", "11:00 AM", "Monday", true),
        StudyTask(3, "English", "Grammar", "02:00 PM", "Monday", false)
    )
    StudyPlannerTheme {
        ScheduleScreenContent(
            tasks = sampleTasks,
            selectedDay = "Monday",
            onTaskClick = {},
            onDaySelected = {},
            onDeleteTask = {},
            onCompleteTask = {},
            onAddTask = { _, _, _ -> }
        )
    }
}
