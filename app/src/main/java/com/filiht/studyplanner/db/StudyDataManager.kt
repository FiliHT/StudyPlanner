package com.filiht.studyplanner.db

import android.content.ContentValues
import com.filiht.studyplanner.model.StudyTask

class StudyDataManager(private val dbHelper: DatabaseHelper) {

    fun addTask(task: StudyTask) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBJECT, task.subject)
            put(DatabaseHelper.COLUMN_TOPIC, task.topic)
            put(DatabaseHelper.COLUMN_TIME, task.time)
            put(DatabaseHelper.COLUMN_DAY, task.day)
            put(DatabaseHelper.COLUMN_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.insert(DatabaseHelper.TABLE_TASKS, null, values)
        db.close()
    }

    fun updateTask(task: StudyTask) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBJECT, task.subject)
            put(DatabaseHelper.COLUMN_TOPIC, task.topic)
            put(DatabaseHelper.COLUMN_TIME, task.time)
            put(DatabaseHelper.COLUMN_DAY, task.day)
            put(DatabaseHelper.COLUMN_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        db.update(
            DatabaseHelper.TABLE_TASKS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(task.id.toString())
        )
        db.close()
    }

    fun updateTaskStatus(taskId: Int, isCompleted: Boolean) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.update(
            DatabaseHelper.TABLE_TASKS,
            values,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(taskId.toString())
        )
        db.close()
    }

    fun getTasksByDay(day: String): List<StudyTask> {
        val tasks = mutableListOf<StudyTask>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_TASKS,
            null,
            "${DatabaseHelper.COLUMN_DAY} = ?",
            arrayOf(day),
            null,
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val subject = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBJECT))
                val topic = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOPIC))
                val time = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME))
                val taskDay = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY))
                val isCompleted = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPLETED)) == 1
                
                tasks.add(StudyTask(id, subject, topic, time, taskDay, isCompleted))
            }
        }
        cursor.close()
        db.close()
        return tasks
    }

    fun getCompletionStats(day: String): Pair<Int, Int> {
        val db = dbHelper.readableDatabase
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_TASKS} WHERE ${DatabaseHelper.COLUMN_DAY} = ?",
            arrayOf(day)
        )
        val completedCursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${DatabaseHelper.TABLE_TASKS} WHERE ${DatabaseHelper.COLUMN_DAY} = ? AND ${DatabaseHelper.COLUMN_COMPLETED} = 1",
            arrayOf(day)
        )

        var total = 0
        var completed = 0

        if (totalCursor.moveToFirst()) total = totalCursor.getInt(0)
        if (completedCursor.moveToFirst()) completed = completedCursor.getInt(0)

        totalCursor.close()
        completedCursor.close()
        db.close()
        return Pair(completed, total)
    }

    fun deleteTask(taskId: Int): Int {
        val db = dbHelper.writableDatabase
        val result = db.delete(
            DatabaseHelper.TABLE_TASKS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(taskId.toString())
        )
        db.close()
        return result
    }

    // Removed incrementStreak
}
