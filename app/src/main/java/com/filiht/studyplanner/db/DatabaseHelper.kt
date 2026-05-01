package com.filiht.studyplanner.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "study_planner.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_TASKS = "study_tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_SUBJECT = "subject"
        const val COLUMN_TOPIC = "topic"
        const val COLUMN_TIME = "time"
        const val COLUMN_DAY = "day"
        const val COLUMN_COMPLETED = "is_completed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE $TABLE_TASKS (" +
                "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_SUBJECT TEXT," +
                "$COLUMN_TOPIC TEXT," +
                "$COLUMN_TIME TEXT," +
                "$COLUMN_DAY TEXT," +
                "$COLUMN_COMPLETED INTEGER" +
                ")")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }
}
