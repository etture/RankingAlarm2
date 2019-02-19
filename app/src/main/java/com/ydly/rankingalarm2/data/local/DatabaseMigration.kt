package com.ydly.rankingalarm2.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration

val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Boolean in SQLite is Integer with 0=false, 1=true
        database.execSQL("ALTER TABLE alarmData ADD COLUMN isToggledOn INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS alarmHistoryData")
        database.execSQL("CREATE TABLE alarmHistoryData (" +
                "id INTEGER PRIMARY KEY," +
                "year INTEGER NOT NULL," +
                "month INTEGER NOT NULL," +
                "dayOfMonth INTEGER NOT NULL," +
                "alarmTimeInMillis INTEGER," +
                "takenTimeInMillis INTEGER," +
                "rangToday INTEGER NOT NULL DEFAULT 0" +
                ")")
        database.execSQL("CREATE UNIQUE INDEX index_alarmHistoryData_year_month_dayOfMonth ON alarmHistoryData(year, month, dayOfMonth)")
    }
}