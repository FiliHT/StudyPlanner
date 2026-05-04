package com.filiht.studyplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme

@Composable
fun SettingsScreen(
    viewModel: StudyViewModel,
    onBack: () -> Unit
) {
    var showFocusDialog by remember { mutableStateOf(false) }
    var showBreakDialog by remember { mutableStateOf(false) }

    SettingsScreenContent(
        focusDurationMinutes = viewModel.focusDurationMinutes.value,
        breakDurationMinutes = viewModel.breakDurationMinutes.value,
        isNotificationsEnabled = viewModel.isNotificationsEnabled.value,
        isDarkMode = viewModel.isDarkMode.value,
        onBack = onBack,
        onFocusDurationClick = { showFocusDialog = true },
        onBreakDurationClick = { showBreakDialog = true },
        onNotificationsToggle = { viewModel.isNotificationsEnabled.value = it },
        onDarkModeToggle = { viewModel.isDarkMode.value = it }
    )

    if (showFocusDialog) {
        DurationDialog(
            title = "Focus Duration",
            initialValue = viewModel.focusDurationMinutes.value.toString(),
            onDismiss = { showFocusDialog = false },
            onConfirm = { 
                viewModel.setTimerSettings(it.toLong(), viewModel.breakDurationMinutes.value)
                showFocusDialog = false
            }
        )
    }

    if (showBreakDialog) {
        DurationDialog(
            title = "Break Duration",
            initialValue = viewModel.breakDurationMinutes.value.toString(),
            onDismiss = { showBreakDialog = false },
            onConfirm = { 
                viewModel.setTimerSettings(viewModel.focusDurationMinutes.value, it.toLong())
                showBreakDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    focusDurationMinutes: Long,
    breakDurationMinutes: Long,
    isNotificationsEnabled: Boolean,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onFocusDurationClick: () -> Unit,
    onBreakDurationClick: () -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            SettingsSection(title = "STUDY PREFERENCES") {
                SettingsItem(
                    icon = Icons.Outlined.Schedule,
                    iconBgColor = Color(0xFFE8E9FF).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFF5D5FEF),
                    label = "Focus Duration",
                    value = "$focusDurationMinutes min",
                    onClick = onFocusDurationClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Refresh,
                    iconBgColor = Color(0xFFE6F6F3).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFF00A389),
                    label = "Break Duration",
                    value = "$breakDurationMinutes min",
                    valueColor = Color(0xFF00A389),
                    onClick = onBreakDurationClick
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsToggleItem(
                    icon = Icons.Outlined.Notifications,
                    iconBgColor = Color(0xFFFFF2E8).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFFFF8C39),
                    label = "Notifications",
                    checked = isNotificationsEnabled,
                    onCheckedChange = onNotificationsToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "APPEARANCE") {
                SettingsToggleItem(
                    icon = Icons.Outlined.DarkMode,
                    iconBgColor = Color(0xFFE8F0FF).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFF5D5FEF),
                    label = "Dark Mode",
                    checked = isDarkMode,
                    onCheckedChange = onDarkModeToggle
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            SettingsSection(title = "ABOUT") {
                SettingsItem(
                    icon = Icons.Outlined.Person,
                    iconBgColor = Color(0xFFF0F2FF).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFF5D5FEF),
                    label = "Creator",
                    value = "Filiツ",
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    iconBgColor = Color(0xFFF0F2FF).copy(alpha = if(isDarkMode) 0.15f else 1f),
                    iconColor = Color(0xFF5D5FEF),
                    label = "App Version",
                    value = "v1.0.0",
                    valueColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DurationDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
                label = { Text("Minutes") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { if (text.isNotEmpty()) onConfirm(text) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    label: String,
    value: String,
    valueColor: Color = Color(0xFF5D5FEF),
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconBgColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = iconBgColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF5D5FEF),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE9E9E9),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    StudyPlannerTheme {
        SettingsScreenContent(
            focusDurationMinutes = 25,
            breakDurationMinutes = 5,
            isNotificationsEnabled = true,
            isDarkMode = false,
            onBack = {},
            onFocusDurationClick = {},
            onBreakDurationClick = {},
            onNotificationsToggle = {},
            onDarkModeToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenDarkPreview() {
    StudyPlannerTheme(darkTheme = true) {
        SettingsScreenContent(
            focusDurationMinutes = 25,
            breakDurationMinutes = 5,
            isNotificationsEnabled = true,
            isDarkMode = true,
            onBack = {},
            onFocusDurationClick = {},
            onBreakDurationClick = {},
            onNotificationsToggle = {},
            onDarkModeToggle = {}
        )
    }
}
