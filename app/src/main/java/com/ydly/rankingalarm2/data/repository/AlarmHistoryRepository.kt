package com.ydly.rankingalarm2.data.repository

import com.ydly.rankingalarm2.base.BaseRepository
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryData
import org.jetbrains.anko.info
import javax.inject.Inject

class AlarmHistoryRepository : BaseRepository() {

    @Inject
    lateinit var alarmHistoryDao: AlarmHistoryDao

    @Inject
    lateinit var alarmDataDao: AlarmHistoryDao

    init {
        info(alarmDataDao.toString())
    }

    private fun _newDay(year: Int, month: Int, dayOfMonth: Int): AlarmHistoryData {
        val newDay = AlarmHistoryData(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth
        )
        val insertId = alarmHistoryDao.insert(newDay)
        return newDay.apply { id = insertId }
    }


    fun newDay(year: Int, month: Int, dayOfMonth: Int): AlarmHistoryData {
        return _newDay(year, month, dayOfMonth)
    }
}