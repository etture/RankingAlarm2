package com.ydly.rankingalarm2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmDataDao
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmHistoryData

@Database(entities = [AlarmData::class, AlarmHistoryData::class], version = 7, exportSchema = true)
abstract class RankingAlarmDatabase : RoomDatabase() {

    abstract fun alarmDataDao(): AlarmDataDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao

}
