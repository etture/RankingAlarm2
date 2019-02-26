package com.ydly.rankingalarm2.data.repository

import com.ydly.rankingalarm2.base.BaseRepository
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmHistoryData
import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.AlarmRetrofitService
import com.ydly.rankingalarm2.data.remote.model.response.SampleResponse
import com.ydly.rankingalarm2.data.remote.model.response.UploadResponse
import io.reactivex.Flowable
import org.jetbrains.anko.info
import retrofit2.Response
import java.util.*
import javax.inject.Inject

class AlarmHistoryRepository : BaseRepository() {

    @Inject
    lateinit var alarmHistoryDao: AlarmHistoryDao

    @Inject
    lateinit var alarmRetrofitService: AlarmRetrofitService

    init {
        info(alarmHistoryDao.toString())
    }


    //========= Internal functions with DB access via DAO ==========

    // Try to insert the alarmHistoryData into local DB
    // If it succeeds, then send it to the server as well
    // If not, then don't even bother with the server
    private fun _insertAlarmHistory(
        alarmTimeInMillis: Long,
        takenTimeInMillis: Long?,
        hour: Int,
        minute: Int,
        wokeUp: Boolean
    ): Long {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarmTimeInMillis

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val baseTimeInMillis = calendar.timeInMillis
        val timeZoneId = calendar.timeZone.id

        val alarmHistory = AlarmHistoryData(
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hour = hour,
            minute = minute,
            timeZoneId = timeZoneId,
            baseTimeInMillis = baseTimeInMillis,
            alarmTimeInMillis = alarmTimeInMillis,
            takenTimeInMillis = takenTimeInMillis,
            wokeUp = wokeUp
        )

        // Get insertId from inserting into local DB
        // insertId == 1 means alarm has already been stored locally for today
        // Send info to SERVER if insertId is not -1
        val insertId = alarmHistoryDao.insert(alarmHistory)

        info("_insertAlarmHistory() -> new AlarmHistoryData: $alarmHistory, insertId: $insertId")

        return insertId
    }

    private fun _uploadAlarmHistory(alarmHistoryBody: AlarmHistoryBody): Flowable<Response<UploadResponse>> {
        // Send via POST to server
        return alarmRetrofitService.uploadAlarmHistory(alarmHistoryBody)
    }

    private fun _getToday(year: Int, month: Int, dayOfMonth: Int): Flowable<List<AlarmHistoryData>> {
        return alarmHistoryDao.getToday(year, month, dayOfMonth)
    }


    //========= Functions accessible by ViewModel ==========

    fun insertAlarmHistory(alarmTimeInMillis: Long, takenTimeInMillis: Long?, hour: Int, minute: Int, wokeUp: Boolean): Long {
        return _insertAlarmHistory(alarmTimeInMillis, takenTimeInMillis, hour, minute, wokeUp)
    }

    fun uploadAlarmHistory(alarmHistoryBody: AlarmHistoryBody): Flowable<Response<UploadResponse>> {
        return _uploadAlarmHistory(alarmHistoryBody)
    }

    fun testHeader(): Flowable<SampleResponse> {
        info("testHeader()")
        return alarmRetrofitService.testHeader()
    }

    fun getToday(year: Int, month: Int, dayOfMonth: Int): Flowable<List<AlarmHistoryData>> {
        return _getToday(year, month, dayOfMonth)
    }

    fun deleteAllLocal() {
        alarmHistoryDao.deleteAll()
    }

}