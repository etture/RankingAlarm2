package com.ydly.rankingalarm2.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Boolean in SQLite is Integer with 0=false, 1=true
        // Add column isToggledOn to table alarmData
        database.execSQL("ALTER TABLE alarmData ADD COLUMN isToggledOn INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create table alarmHistoryData
        database.execSQL("DROP TABLE IF EXISTS alarmHistoryData")
        database.execSQL(
            "CREATE TABLE alarmHistoryData (" +
                    "id INTEGER PRIMARY KEY," +
                    "year INTEGER NOT NULL," +
                    "month INTEGER NOT NULL," +
                    "dayOfMonth INTEGER NOT NULL," +
                    "alarmTimeInMillis INTEGER," +
                    "takenTimeInMillis INTEGER," +
                    "rangToday INTEGER NOT NULL DEFAULT 0" +
                    ")"
        )
        database.execSQL("CREATE UNIQUE INDEX index_alarmHistoryData_year_month_dayOfMonth ON alarmHistoryData(year, month, dayOfMonth)")
    }
}

val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Rename column rangToday in alarmHistoryData to wokeUp
        try {
            database.beginTransaction()
            database.execSQL("ALTER TABLE alarmHistoryData RENAME TO tempHistoryData")
            database.execSQL(
                "CREATE TABLE alarmHistoryData (" +
                        "id INTEGER PRIMARY KEY," +
                        "year INTEGER NOT NULL," +
                        "month INTEGER NOT NULL," +
                        "dayOfMonth INTEGER NOT NULL," +
                        "alarmTimeInMillis INTEGER," +
                        "takenTimeInMillis INTEGER," +
                        "wokeUp INTEGER NOT NULL DEFAULT 0" +
                        ")"
            )
            database.execSQL("DROP INDEX index_alarmHistoryData_year_month_dayOfMonth")
            database.execSQL("CREATE UNIQUE INDEX index_alarmHistoryData_year_month_dayOfMonth ON alarmHistoryData(year, month, dayOfMonth)")
            database.execSQL(
                "INSERT INTO alarmHistoryData (id, year, month, dayOfMonth, alarmTimeInMillis, takenTimeInMillis, wokeUp) " +
                        "SELECT id, year, month, dayOfMonth, alarmTimeInMillis, takenTimeInMillis, rangToday " +
                        "FROM tempHistoryData"
            )
            database.execSQL("DROP TABLE tempHistoryData")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add timeZoneId, baseTimeInMillis to alarmHistoryData
        try {
            database.beginTransaction()
            database.execSQL("DROP INDEX index_alarmHistoryData_year_month_dayOfMonth")
            database.execSQL("DROP TABLE alarmHistoryData")
            database.execSQL(
                "CREATE TABLE alarmHistoryData (" +
                        "id INTEGER PRIMARY KEY," +
                        "year INTEGER NOT NULL," +
                        "month INTEGER NOT NULL," +
                        "dayOfMonth INTEGER NOT NULL," +
                        "timeZoneId TEXT NOT NULL," +
                        "baseTimeInMillis INTEGER NOT NULL," +
                        "alarmTimeInMillis INTEGER NOT NULL," +
                        "takenTimeInMillis INTEGER," +
                        "wokeUp INTEGER NOT NULL DEFAULT 0" +
                        ")"
            )
            database.execSQL("CREATE UNIQUE INDEX index_alarmHistoryData_year_month_dayOfMonth ON alarmHistoryData(year, month, dayOfMonth)")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }
}