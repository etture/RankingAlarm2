package com.ydly.rankingalarm2.data.local

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.migration.Migration

val MIGRATION_1_2: Migration = object: Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE alarmData ADD COLUMN isToggledOn INTEGER NOT NULL DEFAULT 0")
    }
}