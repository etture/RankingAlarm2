package com.ydly.rankingalarm2.data.repository

import android.content.SharedPreferences
import com.ydly.rankingalarm2.base.BaseRepository
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryData
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

class AlarmHistoryRepository : BaseRepository() {

    @Inject
    lateinit var alarmHistoryDao: AlarmHistoryDao

    init {
        info(alarmHistoryDao.toString())
    }


    //========= Internal functions with DB access via DAO ==========

//    private fun _newDay(year: Int, month: Int, dayOfMonth: Int): AlarmHistoryData {
//        val newDay = AlarmHistoryData(
//            year = year,
//            month = month,
//            dayOfMonth = dayOfMonth
//        )
//        val insertId = alarmHistoryDao.insert(newDay)
//        return newDay.apply { id = insertId }
//    }

    private fun _insertAlarmHistory(alarmTimeInMillis: Long, takenTimeInMillis: Long?, wokeUp: Boolean): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarmTimeInMillis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val alarmHistory = AlarmHistoryData(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            alarmTimeInMillis = alarmTimeInMillis,
            takenTimeInMillis = takenTimeInMillis,
            wokeUp = wokeUp
        )

        info("_insertAlarmHistory() -> new AlarmHistoryData: $alarmHistory")

        return alarmHistoryDao.insert(alarmHistory)
    }


    //========= Functions accessible by ViewModel ==========

//    fun newDay(year: Int, month: Int, dayOfMonth: Int): AlarmHistoryData {
//        return _newDay(year, month, dayOfMonth)
//    }

    fun insertAlarmHistory(alarmTimeInMillis: Long, takenTimeInMillis: Long?, wokeUp: Boolean): Long {
        return _insertAlarmHistory(alarmTimeInMillis, takenTimeInMillis, wokeUp)
    }

}