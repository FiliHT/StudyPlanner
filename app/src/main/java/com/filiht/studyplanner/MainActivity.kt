package com.filiht.studyplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.filiht.studyplanner.db.DatabaseHelper
import com.filiht.studyplanner.db.StudyDataManager
import com.filiht.studyplanner.model.StudyTask
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
            StudyPlannerTheme(darkTheme = viewModel.isDarkMode.value) {
                StudyPlannerApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun StudyPlannerApp(viewModel: StudyViewModel) {
    val navController = rememberNavController()
    // Create a state to hold the task being studied
    var activeTask by remember { mutableStateOf<StudyTask?>(null) }

    NavHost(navController = navController, startDestination = "schedule") {
        composable("schedule") {
            ScheduleScreen(
                viewModel = viewModel,
                onTaskClick = { task ->
                    if (!task.isCompleted) {
                        activeTask = task // Store the clicked task
                        navController.navigate("timer")
                    }
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }
        composable("timer") {
            // Pass the activeTask to the TimerScreen
            TimerScreen(
                task = activeTask,
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
