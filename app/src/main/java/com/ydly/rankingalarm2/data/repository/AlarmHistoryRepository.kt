package com.ydly.rankingalarm2.data.repository

import com.ydly.rankingalarm2.base.BaseRepository
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmHistoryDao
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmHistoryData
import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.AlarmRetrofitService
import com.ydly.rankingalarm2.data.remote.model.response.NumPeopleResponse
import com.ydly.rankingalarm2.data.remote.model.response.PendingListResponse
import com.ydly.rankingalarm2.data.remote.model.response.SampleResponse
import com.ydly.rankingalarm2.data.remote.model.response.UploadResponse
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
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

    private fun _uploadPendingHistoryList(pendingHistoryList: List<AlarmHistoryBody>): Flowable<Response<PendingListResponse>> {
        return alarmRetrofitService.uploadPendingHistoryList(pendingHistoryList)
    }

    private fun _updateRank(originalId: Long, dayRank: Int, morningRank: Int) {
        alarmHistoryDao.updateRank(originalId, dayRank, morningRank)
    }

    private fun _bulkUpdateRanks(bulkUpdateList: List<Triple<Long, Int, Int>>) {
        for (item in bulkUpdateList) {
            alarmHistoryDao.updateRank(
                originalId = item.first,
                dayRank = item.second,
                morningRank = item.third
            )
        }
    }

    private fun _fetchNumPeople(year: Int, month: Int, dayOfMonth: Int): Flowable<Response<NumPeopleResponse>> {
        return alarmRetrofitService.fetchNumPeople(year, month, dayOfMonth)
    }

    private fun _updateNumPeople(year: Int, month: Int, dayOfMonth: Int, dayNumPeople: Int, morningNumPeople: Int) {
        alarmHistoryDao.updateNumPeople(year, month, dayOfMonth, dayNumPeople, morningNumPeople)
    }

    private fun _getOneDay(year: Int, month: Int, dayOfMonth: Int): Flowable<List<AlarmHistoryData>> {
        return alarmHistoryDao.getOneDay(year, month, dayOfMonth)
    }


    //========= Functions accessible by ViewModel ==========

    fun insertAlarmHistory(
        alarmTimeInMillis: Long,
        takenTimeInMillis: Long?,
        hour: Int,
        minute: Int,
        wokeUp: Boolean
    ): Long {
        return _insertAlarmHistory(alarmTimeInMillis, takenTimeInMillis, hour, minute, wokeUp)
    }

    fun uploadAlarmHistory(alarmHistoryBody: AlarmHistoryBody): Flowable<Response<UploadResponse>> {
        return _uploadAlarmHistory(alarmHistoryBody)
    }

    fun uploadPendingHistoryList(pendingHistoryList: List<AlarmHistoryBody>): Flowable<Response<PendingListResponse>> {
        return _uploadPendingHistoryList(pendingHistoryList)
    }

    fun updateRank(originalId: Long, dayRank: Int, morningRank: Int) {
        _updateRank(originalId, dayRank, morningRank)
    }

    fun bulkUpdateRanks(bulkUpdateList: List<Triple<Long, Int, Int>>) {
        _bulkUpdateRanks(bulkUpdateList)
    }

    fun fetchNumPeople(year: Int, month: Int, dayOfMonth: Int): Flowable<Response<NumPeopleResponse>> {
        return _fetchNumPeople(year, month, dayOfMonth)
    }

    fun updateNumPeople(year: Int, month: Int, dayOfMonth: Int, dayNumPeople: Int, morningNumPeople: Int) {
        _updateNumPeople(year, month, dayOfMonth, dayNumPeople, morningNumPeople)
    }

    fun testHeader(): Flowable<SampleResponse> {
        info("testHeader()")
        return alarmRetrofitService.testHeader()
    }

    fun getOneDay(year: Int, month: Int, dayOfMonth: Int): Flowable<List<AlarmHistoryData>> {
        return _getOneDay(year, month, dayOfMonth)
    }

    fun deleteAllLocal() {
        alarmHistoryDao.deleteAll()
    }

}
