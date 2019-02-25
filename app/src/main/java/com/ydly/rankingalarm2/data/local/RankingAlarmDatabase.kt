package com.ydly.rankingalarm2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.local.alarm.AlarmDataDao
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryData

@Database(entities = [AlarmData::class, AlarmHistoryData::class], version = 6, exportSchema = true)
abstract class RankingAlarmDatabase : RoomDatabase() {

    abstract fun alarmDataDao(): AlarmDataDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao

}
