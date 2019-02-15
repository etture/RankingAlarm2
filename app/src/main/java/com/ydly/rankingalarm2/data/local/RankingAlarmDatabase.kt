package com.ydly.rankingalarm2.data.local

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.local.alarm.AlarmDataDao

@Database(entities = [AlarmData::class], version = 2, exportSchema = false)
abstract class RankingAlarmDatabase : RoomDatabase() {

    abstract fun alarmDataDao(): AlarmDataDao

}
