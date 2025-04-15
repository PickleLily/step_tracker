package edu.wpi.cs.cs4518.stepcounter_starter

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StepDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "step_database"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "step_data"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_STEPS = "steps"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_STEPS INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertStep(timestamp: Long, steps: Int) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TIMESTAMP, timestamp)
            put(COLUMN_STEPS, steps)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getStepsForDay(start: Long, end: Long): List<StepData> {
        val stepsList = mutableListOf<StepData>()
        val db = readableDatabase
        val query = """
            SELECT $COLUMN_ID, $COLUMN_TIMESTAMP, $COLUMN_STEPS 
            FROM $TABLE_NAME 
            WHERE $COLUMN_TIMESTAMP >= ? AND $COLUMN_TIMESTAMP < ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(start.toString(), end.toString()))
        while (cursor.moveToNext()) {
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            val steps = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STEPS))
            stepsList.add(StepData(id, timestamp, steps))
        }
        cursor.close()
        db.close()
        return stepsList
    }

    fun getTotalStepsForDay(start: Long, end: Long): Int {
        val db = readableDatabase
        val query = """
            SELECT SUM($COLUMN_STEPS) 
            FROM $TABLE_NAME 
            WHERE $COLUMN_TIMESTAMP >= ? AND $COLUMN_TIMESTAMP < ?
        """.trimIndent()
        val cursor = db.rawQuery(query, arrayOf(start.toString(), end.toString()))
        var totalSteps = 0
        if (cursor.moveToFirst()) {
            totalSteps = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return totalSteps
    }

    fun clearAllData() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}