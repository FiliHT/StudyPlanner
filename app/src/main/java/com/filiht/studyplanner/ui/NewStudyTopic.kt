package com.filiht.studyplanner.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewStudySessionModal(
    onDismiss: () -> Unit,
    onAdd: (subject: String, topic: String, time: String) -> Unit
) {
    var selectedSubject by remember { mutableStateOf("") }
    var topicName by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }

    fun formatTime(hour: Int, minute: Int): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    // Use derivedStateOf to ensure the state reads are correctly tracked and warnings are resolved
    val timeDisplayStr by remember {
        derivedStateOf {
            formatTime(selectedHour, selectedMinute)
        }
    }

    val subjects = listOf("Maths", "Science", "English", "History", "Database")

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            // Fix: Moved the "Create Session" button to the confirmButton slot.
            // This is more idiomatic for AlertDialog and avoids potential layout issues
            // with AlertDialogFlowRow when buttons are empty or non-measurable.
            Button(
                onClick = {
                    if (selectedSubject.isNotBlank() && topicName.isNotBlank()) {
                        onAdd(selectedSubject, topicName, timeDisplayStr)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF))
            ) {
                Text("Add Topic", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = null, // Fix: Use null instead of an empty lambda to avoid layout issues in the button row
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        shape = RoundedCornerShape(32.dp),
        containerColor = Color.White,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Study Topic",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black

                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "SUBJECT",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(subjects) { subject ->
                        val isSelected = selectedSubject == subject
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSubject = subject },
                            label = { Text(subject) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                labelColor = Color.Gray,
                                selectedContainerColor = Color(0xFFE8EAF6),
                                selectedLabelColor = Color(0xFF5D5FEF)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color.Transparent,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 0.dp,
                                selectedBorderWidth = 0.dp
                            )
                        )
                    }
                }

                Text(
                    "TOPIC NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = topicName,
                    onValueChange = { topicName = it },
                    placeholder = { Text("e.g. Calculus Basics", color = Color.LightGray) },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color(0xFF5D5FEF),
                        unfocusedBorderColor = Color(0xFFF0F0F0),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedContainerColor = Color(0xFFF5F5F5)
                    )
                )

                Text(
                    "START TIME",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFF0F0F0), RoundedCornerShape(12.dp))
                        .clickable {
                            // Fix: Instantiate and show TimePickerDialog only when clicked.
                            // This avoids potential issues with non-Compose dialogs during rendering.
                            TimePickerDialog(
                                context,
                                { _, hour: Int, minute: Int ->
                                    selectedHour = hour
                                    selectedMinute = minute
                                },
                                selectedHour,
                                selectedMinute,
                                false
                            ).show()
                        }
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeDisplayStr,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Select Time",
                        tint = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun NewStudySessionModalPreview() {
    StudyPlannerTheme {
        NewStudySessionModal(
            onDismiss = {},
            onAdd = { _, _, _ -> }
        )
    }
}
