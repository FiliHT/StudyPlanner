package com.filiht.studyplanner.db

import android.content.ContentValues
import com.filiht.studyplanner.model.StudyTask
import com.filiht.studyplanner.model.Subject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow

class StudyDataManager(private val dbHelper: DatabaseHelper) {

    fun getAllSubjects(): Flow<List<Subject>> = flow {
        val subjects = mutableListOf<Subject>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            DatabaseHelper.TABLE_SUBJECTS,
            null,
            null,
            null,
            null,
            null,
            "${DatabaseHelper.COLUMN_SUBJECT_NAME} ASC"
        )
        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val name = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBJECT_NAME))
                subjects.add(Subject(id, name))
            }
        }
        cursor.close()
        emit(subjects)
    }

    fun addSubject(name: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBJECT_NAME, name)
        }
        val id = db.insert(DatabaseHelper.TABLE_SUBJECTS, null, values)
        db.close()
        return id
    }

    fun deleteSubject(subjectId: Int): Int {
        val db = dbHelper.writableDatabase
        // First delete all tasks associated with this subject to maintain integrity
        db.delete(
            DatabaseHelper.TABLE_TASKS,
            "${DatabaseHelper.COLUMN_SUBJECT_ID} = ?",
            arrayOf(subjectId.toString())
        )
        val result = db.delete(
            DatabaseHelper.TABLE_SUBJECTS,
            "${DatabaseHelper.COLUMN_ID} = ?",
            arrayOf(subjectId.toString())
        )
        db.close()
        return result
    }

    fun addTask(task: StudyTask): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBJECT_ID, task.subjectId)
            put(DatabaseHelper.COLUMN_TOPIC, task.topic)
            put(DatabaseHelper.COLUMN_TIME, task.time)
            put(DatabaseHelper.COLUMN_DAY, task.day)
            put(DatabaseHelper.COLUMN_COMPLETED, if (task.isCompleted) 1 else 0)
        }
        val id = db.insert(DatabaseHelper.TABLE_TASKS, null, values)
        db.close()
        return id
    }

    fun updateTask(task: StudyTask) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_SUBJECT_ID, task.subjectId)
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
        
        val query = """
            SELECT t.*, s.${DatabaseHelper.COLUMN_SUBJECT_NAME} 
            FROM ${DatabaseHelper.TABLE_TASKS} t
            JOIN ${DatabaseHelper.TABLE_SUBJECTS} s ON t.${DatabaseHelper.COLUMN_SUBJECT_ID} = s.${DatabaseHelper.COLUMN_ID}
            WHERE t.${DatabaseHelper.COLUMN_DAY} = ?
        """.trimIndent()
        
        val cursor = db.rawQuery(query, arrayOf(day))

        with(cursor) {
            while (moveToNext()) {
                val id = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val subjectId = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBJECT_ID))
                val subjectName = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUBJECT_NAME))
                val topic = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOPIC))
                val time = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME))
                val taskDay = getString(getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY))
                val isCompleted = getInt(getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMPLETED)) == 1
                
                tasks.add(StudyTask(id, subjectId, subjectName, topic, time, taskDay, isCompleted))
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
}
