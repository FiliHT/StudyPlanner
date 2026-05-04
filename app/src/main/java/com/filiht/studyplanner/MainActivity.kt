package com.filiht.studyplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.filiht.studyplanner.db.DatabaseHelper
import com.filiht.studyplanner.db.StudyDataManager
import com.filiht.studyplanner.ui.ScheduleScreen
import com.filiht.studyplanner.ui.SettingsScreen
import com.filiht.studyplanner.ui.StudyViewModel
import com.filiht.studyplanner.ui.TimerScreen
import com.filiht.studyplanner.ui.theme.StudyPlannerTheme

class MainActivity : ComponentActivity() {
    
    private val dbHelper by lazy { DatabaseHelper(this) }
    private val dataManager by lazy { StudyDataManager(dbHelper) }
    
    private val viewModel: StudyViewModel by viewModels {
        StudyViewModel.Factory(dataManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            
            // Handle Notification Intent
            LaunchedEffect(intent) {
                val taskId = intent?.getIntExtra("TASK_ID", -1) ?: -1
                if (taskId != -1) {
                    viewModel.getTaskById(taskId) { task ->
                        if (task != null) {
                            viewModel.activeTask.value = task
                            navController.navigate("timer") {
                                // Clear backstack up to schedule to avoid loops
                                popUpTo("schedule") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }

            StudyPlannerTheme(darkTheme = viewModel.isDarkMode.value) {
                StudyPlannerApp(
                    viewModel = viewModel, 
                    navController = navController
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Update the activity intent so LaunchedEffect can detect the change
        setIntent(intent)
    }
}

@Composable
fun StudyPlannerApp(
    viewModel: StudyViewModel, 
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "schedule") {
        composable("schedule") {
            ScheduleScreen(
                viewModel = viewModel,
                onTaskClick = { task ->
                    if (!task.isCompleted) {
                        viewModel.activeTask.value = task
                        navController.navigate("timer")
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        composable("timer") {
            TimerScreen(
                task = viewModel.activeTask.value,
                viewModel = viewModel,
                onBack = { 
                    navController.popBackStack() 
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
