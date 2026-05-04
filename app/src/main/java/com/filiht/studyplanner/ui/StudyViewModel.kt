package com.filiht.studyplanner.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.filiht.studyplanner.db.StudyDataManager
import com.filiht.studyplanner.model.StudyTask
import com.filiht.studyplanner.model.Subject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StudyViewModel(private val dataManager: StudyDataManager) : ViewModel() {

    private val _tasks = MutableStateFlow<List<StudyTask>>(emptyList())
    val tasks: StateFlow<List<StudyTask>> = _tasks.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _selectedDay = MutableStateFlow("Monday")
    val selectedDay: StateFlow<String> = _selectedDay.asStateFlow()

    // Timer state
    var timeLeft = mutableStateOf(25 * 60L) // Current time left in seconds
    var totalTime = mutableStateOf(25 * 60L) // Total time for the current mode
    var isTimerRunning = mutableStateOf(false)
    
    // Task state
    var activeTask = mutableStateOf<StudyTask?>(null)
    var pendingTaskId = mutableStateOf<Int?>(null)
    
    var isFocusMode = mutableStateOf(true)
    var focusDurationMinutes = mutableStateOf(25L)
    var breakDurationMinutes = mutableStateOf(5L)
    
    // New Settings state
    var isNotificationsEnabled = mutableStateOf(true)
    var isDarkMode = mutableStateOf(false)

    private var timerJob: Job? = null

    // Completion stats state
    private val _completionStats = mutableStateOf(Pair(0, 0))
    val completionStats: State<Pair<Int, Int>> = _completionStats

    init {
        loadTasksForDay(_selectedDay.value)
        loadSubjects()
        refreshStats(_selectedDay.value)
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            dataManager.getAllSubjects().collect {
                _subjects.value = it
            }
        }
    }

    fun addSubject(name: String) {
        viewModelScope.launch {
            dataManager.addSubject(name)
            loadSubjects()
        }
    }

    fun deleteSubject(subjectId: Int) {
        viewModelScope.launch {
            dataManager.deleteSubject(subjectId)
            loadSubjects()
            // Reload tasks in case some were deleted
            loadTasksForDay(_selectedDay.value)
            refreshStats(_selectedDay.value)
        }
    }

    fun selectDay(day: String) {
        _selectedDay.value = day
        loadTasksForDay(day)
        refreshStats(day)
    }

    fun refreshStats(day: String) {
        viewModelScope.launch {
            _completionStats.value = dataManager.getCompletionStats(day)
        }
    }

    fun addTask(task: StudyTask, onTaskAdded: (StudyTask) -> Unit = {}) {
        viewModelScope.launch {
            val id = dataManager.addTask(task)
            val savedTask = task.copy(id = id.toInt())
            if (task.day == _selectedDay.value) {
                loadTasksForDay(task.day)
                refreshStats(task.day)
            }
            onTaskAdded(savedTask)
        }
    }

    fun removeTask(taskId: Int) {
        viewModelScope.launch {
            dataManager.deleteTask(taskId)
            loadTasksForDay(_selectedDay.value)
            refreshStats(_selectedDay.value)
        }
    }

    fun onTimerFinished(taskId: Int) {
        viewModelScope.launch {
            dataManager.updateTaskStatus(taskId, true)
            loadTasksForDay(_selectedDay.value)
            refreshStats(_selectedDay.value)
        }
    }

    fun completeTask(task: StudyTask) {
        viewModelScope.launch {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            dataManager.updateTask(updatedTask)
            loadTasksForDay(task.day)
            refreshStats(task.day)
        }
    }

    private fun loadTasksForDay(day: String) {
        viewModelScope.launch {
            _tasks.value = dataManager.getTasksByDay(day)
        }
    }

    fun getTaskById(taskId: Int, callback: (StudyTask?) -> Unit) {
        viewModelScope.launch {
            val task = dataManager.getTaskById(taskId)
            callback(task)
        }
    }
    
    fun setPendingTaskId(taskId: Int) {
        pendingTaskId.value = taskId
    }

    fun startTimer(task: StudyTask? = null) {
        if (task != null) {
            activeTask.value = task
        }
        
        if (isTimerRunning.value) return
        isTimerRunning.value = true
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isTimerRunning.value) {
                if (timeLeft.value > 0) {
                    delay(1000L)
                    timeLeft.value -= 1
                } else {
                    // Timer finished - JUST SWITCH MODES, DON'T AUTO-COMPLETE
                    if (isFocusMode.value) {
                        // Switch to break
                        toggleMode(false)
                    } else {
                        // Switch back to focus
                        toggleMode(true)
                    }
                    // Continue running automatically
                }
            }
        }
    }

    fun toggleMode(focus: Boolean) {
        isFocusMode.value = focus
        val newDuration = if (focus) focusDurationMinutes.value else breakDurationMinutes.value
        timeLeft.value = newDuration * 60L
        totalTime.value = newDuration * 60L
    }

    fun pauseTimer() {
        isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        val duration = if (isFocusMode.value) focusDurationMinutes.value else breakDurationMinutes.value
        timeLeft.value = duration * 60L
        totalTime.value = duration * 60L
    }

    fun setTimerSettings(focusMin: Long, breakMin: Long) {
        focusDurationMinutes.value = focusMin
        breakDurationMinutes.value = breakMin
        // Reset current timer with new settings
        resetTimer()
    }

    fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%02d:%02d".format(minutes, remainingSeconds)
    }

    class Factory(private val dataManager: StudyDataManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StudyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StudyViewModel(dataManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
