package com.filiht.studyplanner.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "study_planner.db"
        private const val DATABASE_VERSION = 2 // Incremented version

        const val TABLE_TASKS = "study_tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_SUBJECT_ID = "subject_id"
        const val COLUMN_TOPIC = "topic"
        const val COLUMN_TIME = "time"
        const val COLUMN_DAY = "day"
        const val COLUMN_COMPLETED = "is_completed"

        const val TABLE_SUBJECTS = "subjects"
        const val COLUMN_SUBJECT_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createSubjectsTable = ("CREATE TABLE $TABLE_SUBJECTS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SUBJECT_NAME TEXT UNIQUE" +
                ")")
        db.execSQL(createSubjectsTable)

        val createTasksTable = ("CREATE TABLE $TABLE_TASKS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SUBJECT_ID INTEGER," +
                "$COLUMN_TOPIC TEXT," +
                "$COLUMN_TIME TEXT," +
                "$COLUMN_DAY TEXT," +
                "$COLUMN_COMPLETED INTEGER," +
                "FOREIGN KEY($COLUMN_SUBJECT_ID) REFERENCES $TABLE_SUBJECTS($COLUMN_ID)" +
                ")")
        db.execSQL(createTasksTable)

        seedSubjects(db)
    }

    private fun seedSubjects(db: SQLiteDatabase) {
        val defaultSubjects = listOf("Maths", "Biology", "English", "History", "Geography")
        for (subject in defaultSubjects) {
            val values = ContentValues().apply {
                put(COLUMN_SUBJECT_NAME, subject)
            }
            db.insert(TABLE_SUBJECTS, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SUBJECTS")
        onCreate(db)
    }
}
