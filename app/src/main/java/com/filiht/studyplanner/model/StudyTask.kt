package com.filiht.studyplanner.model

data class StudyTask(
    val id: Int = 0,
    val subject: String,
    val topic: String,
    val time: String,
    val day: String,
    val isCompleted: Boolean
)
