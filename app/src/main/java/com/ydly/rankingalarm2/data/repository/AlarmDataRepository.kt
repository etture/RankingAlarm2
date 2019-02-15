package com.ydly.rankingalarm2.data.repository

import com.ydly.rankingalarm2.base.BaseRepository
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.local.alarm.AlarmDataDao
import io.reactivex.Flowable
import org.jetbrains.anko.info
import javax.inject.Inject

class AlarmDataRepository : BaseRepository() {

    @Inject
    lateinit var alarmDataDao: AlarmDataDao

    init {
        info(alarmDataDao.toString())
    }

    private fun _getAlarms(): Flowable<List<AlarmData>> {
        return alarmDataDao.getAll()
    }

    private fun _insertNewAlarm(timeInMillis: Long): AlarmData {
        val alarmData = AlarmData(timeInMillis = timeInMillis, isToggledOn = true)
        // If new item, then insertId and if existing, then -1
        val insertId: Long = alarmDataDao.insert(alarmData)
        info("Alarm Set: $alarmData, insertId: $insertId")
        alarmData.id = insertId
        return alarmData
    }

    private fun _updateAlarm(originalAlarmData: AlarmData, editedTimeInMillis: Long): Pair<AlarmData, AlarmData> {
        val newAlarmData = AlarmData(
            id = originalAlarmData.id,
            timeInMillis = editedTimeInMillis,
            isToggledOn = originalAlarmData.isToggledOn
        )
        if (!newAlarmData.isToggledOn) newAlarmData.isToggledOn = true
        alarmDataDao.update(newAlarmData)
        info("Alarm Item Updated: $newAlarmData")
        return Pair(originalAlarmData, newAlarmData)
    }

    private fun _toggleChange(originalAlarmData: AlarmData, isToggledOn: Boolean): Pair<AlarmData, AlarmData> {
        val newAlarmData = AlarmData(
            id = originalAlarmData.id,
            timeInMillis = originalAlarmData.timeInMillis,
            isToggledOn = isToggledOn
        )
        alarmDataDao.update(newAlarmData)
        info("Alarm Item Updated: $newAlarmData")
        return Pair(originalAlarmData, newAlarmData)
    }

    private fun _updateDatesToNextDay(ids: List<Long>) {
        alarmDataDao.updateDatesToNextDay(ids)
        info("Alarm Items Date Set to Next Day, ids: $ids")
    }

    private fun _deleteAlarm(alarmData: AlarmData): AlarmData {
        alarmDataDao.delete(alarmData)
        info("Alarm Item Deleted: $alarmData")
        return alarmData
    }

    private fun _deleteAllAlarms() {
        alarmDataDao.deleteAll()
        info("All Alarm Items Deleted")
    }

    //========= Functions accessible by ViewModel ==========

    fun getAlarms(): Flowable<List<AlarmData>> {
        return _getAlarms()
    }

    // Return the newly added AlarmData object
    fun insertNewAlarm(timeInMillis: Long): AlarmData {
        return _insertNewAlarm(timeInMillis)
    }

    // Return the updated AlarmData object
    fun updateAlarm(originalAlarmData: AlarmData, editedTimeInMillis: Long): Pair<AlarmData, AlarmData> {
        return _updateAlarm(originalAlarmData, editedTimeInMillis)
    }

    fun toggleChange(originalAlarmData: AlarmData, isToggledOn: Boolean): Pair<AlarmData, AlarmData> {
        return _toggleChange(originalAlarmData, isToggledOn)
    }

    fun updateDatesToNextDay(ids: List<Long>) {
        _updateDatesToNextDay(ids)
    }

    fun deleteAlarm(alarmData: AlarmData): AlarmData {
        return _deleteAlarm(alarmData)
    }

    fun deleteAllAlarms() {
        _deleteAllAlarms()
    }

}
