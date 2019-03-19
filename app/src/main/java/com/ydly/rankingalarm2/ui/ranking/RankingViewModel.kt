package com.ydly.rankingalarm2.ui.ranking

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.model.response.ErrorResponse
import com.ydly.rankingalarm2.data.remote.model.response.UploadResponse
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.PENDING_ALARM_HISTORY_JSON
import com.ydly.rankingalarm2.util.REFRESH_OFF
import com.ydly.rankingalarm2.util.SingleEvent
import com.ydly.rankingalarm2.util.extension.fromJson
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RankingViewModel : BaseViewModel() {

    @Inject lateinit var alarmHistoryRepo: AlarmHistoryRepository
    @Inject lateinit var mainPrefs: SharedPreferences
    @Inject lateinit var gson: Gson

    private val refreshEvent = MutableLiveData<SingleEvent<Boolean>>()

    //========= Init and private functions (business logic) ==========

    private fun uploadPendingHistory() {

        val pendingHistoryJsonArray = mainPrefs.getString(PENDING_ALARM_HISTORY_JSON, "[]")
        val pendingHistoryList: MutableList<AlarmHistoryBody> =
            gson.fromJson<MutableList<AlarmHistoryBody>>(pendingHistoryJsonArray!!)
        info("historyJsonArray (length: ${pendingHistoryList.size}): $pendingHistoryJsonArray")

        // No pending alarmHistory items
        if (pendingHistoryList.isEmpty()) {
            info("pendingHistoryList is empty")
            refreshOff()
        }
        // Pending alarmHistory items exist
        else {

            subscription += alarmHistoryRepo.uploadPendingHistoryList(pendingHistoryList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onNext = { response ->
                        if (response.isSuccessful) {

                            val message = response.body()?.message
                            val rankInfos: List<UploadResponse>? = response.body()?.rankInfos
                            val successIdInDeviceList: List<Long>? = response.body()?.successIdInDeviceList

                            info("uploadPendingHistoryList() -> message: $message, rankInfos?.size: ${rankInfos?.size}")

                            val bulkUpdateList = mutableListOf<Triple<Long, Int, Int>>()

                            for (rankInfo in rankInfos!!) {
                                val originalId = rankInfo.originalId
                                val dayRank = rankInfo.dayRank
                                val morningRank = rankInfo.morningRank
                                info("uploadPendingHistoryList() -> originalId: $originalId, dayRank: $dayRank, morningRank: $morningRank")
                                bulkUpdateList.add(Triple(originalId, dayRank, morningRank))
                            }

                            // Remove successfully uploaded pending alarmHistory items from pendingHistoryList
                            // This includes items that were "Duplicate entry" in the server
                            for (successIdInDevice in successIdInDeviceList!!) {
                                pendingHistoryList.removeAll {
                                    it.idInDevice == successIdInDevice
                                }
                            }

                            // Put the remaining (or empty) pendingHistoryList back into mainPrefs
                            // After serializing it into JSON to be saved as String
                            val historyListJson = gson.toJsonTree(pendingHistoryList)
                            info("historyListJson: $historyListJson")
                            if (historyListJson.isJsonArray) {
                                val newHistoryJsonArray = historyListJson.asJsonArray

                                val editor = mainPrefs.edit()
                                editor.putString(PENDING_ALARM_HISTORY_JSON, newHistoryJsonArray.toString())
                                editor.apply()

                                info("new JSON array put into prefs: $newHistoryJsonArray")
                            } else {
                                info("historyListJson NOT a valid JsonArray")
                            }

                            // Bulk update the ranks received from the serve into local DB
                            Flowable.fromCallable { alarmHistoryRepo.bulkUpdateRanks(bulkUpdateList) }
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeBy(
                                    onNext = {
                                        info("bulkUpdateRanks() -> success")
                                    },
                                    onError = { error ->
                                        info("bulkUpdateRanks() -> error: $error")
                                    }
                                )
                        }
                        // Response has error
                        else {

                            val statusCode = response.code()
                            val jsonErrorObj = JSONObject(response.errorBody()?.string())
                            val mError: ErrorResponse =
                                gson.fromJson(jsonErrorObj.toString(), ErrorResponse::class.java)

                            info(
                                "uploadAlarmHistory() -> statusCode: $statusCode, mError.message: ${mError.message}"
                            )

                        }
                    },
                    onError = { error ->
                        refreshOff()
                        info("uploadAlarmHistory() -> error: $error")
                    },
                    onComplete = { refreshOff() }
                )
        }
    }

    private fun refreshOff() {
        refreshEvent.value = SingleEvent(REFRESH_OFF)
    }

    private fun fetchAndUpdateNumPeople(year: Int, month: Int, dayOfMonth: Int) {

        subscription += alarmHistoryRepo.fetchNumPeople(year, month, dayOfMonth)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { response ->
                    if(response.isSuccessful) {

                        val returnedYear = response.body()?.year
                        val returnedMonth = response.body()?.month
                        val returnedDayOfMonth = response.body()?.dayOfMonth
                        val dayNumPeople = response.body()?.dayNumPeople
                        val morningNumPeople = response.body()?.morningNumPeople

                        info("fetchAndUpdateNumPeople() -> fetch success year: $returnedYear, month: $returnedMonth, dayOfMonth: $returnedDayOfMonth, dayNumPeople: $dayNumPeople, morningNumPeople: $morningNumPeople")

                        Flowable.fromCallable {
                            alarmHistoryRepo.updateNumPeople(
                                year = returnedYear!!,
                                month = returnedMonth!!,
                                dayOfMonth = returnedDayOfMonth!!,
                                dayNumPeople = dayNumPeople!!,
                                morningNumPeople = morningNumPeople!!
                            )
                        }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onNext = {
                                    info("fetchAndUpdateNumPeople() -> update success")
                                },
                                onError = { error ->
                                    info("fetchAndUpdateNumPeople() -> update fail, error: $error")
                                }
                            )

                    } else {

                        val statusCode = response.code()
                        val jsonErrorObj = JSONObject(response.errorBody()?.string())
                        val mError: ErrorResponse =
                            gson.fromJson(jsonErrorObj.toString(), ErrorResponse::class.java)
                        info("fetchAndUpdateNumPeople() -> fetch error from server, statusCode: $statusCode, error message: ${mError.message}")
                    }

                },
                onError = { error ->
                    info("fetchAndUpdateNumPeople() -> update fail, error: $error")
                }
            )
    }


    //========= Functions accessible by View (data manipulation) ==========

    // Observed by View to turn swipeRefresh back off
    fun observeRefreshEvent(): LiveData<SingleEvent<Boolean>> = refreshEvent

    // Attempt to upload the pending alarmHistory items stored in mainPrefs
    // Called by Fragment onResume() and SwipeRefreshLayout
    fun attemptUploadPendingHistory() {
        info("attemptUploadPendingHistory()")
        uploadPendingHistory()
    }

    fun updateLatestNumPeople(year: Int, month: Int, dayOfMonth: Int) {
        info("fetchNumPeople()")
        fetchAndUpdateNumPeople(year, month, dayOfMonth)
    }

    fun clearSubscription() {
        subscription.clear()
    }

    //========= Functions accessible by View (DataBinding) ==========


    //========= Functions accessible by View (TESTING) ==========

    fun swipeTest() {
        info("swipeTest()")
        subscription += Flowable.timer(4, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    info("delayed 4 sec")
                    refreshEvent.value = SingleEvent(false)
                },
                onError = {
                    info("delay error")
                }
            )
    }

    fun deleteAllLocal() {
        subscription += Flowable.fromCallable { alarmHistoryRepo.deleteAllLocal() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    info("all alarmHistoryData deleted")
                },
                onError = {}
            )
    }

    fun newUUID() {
        val editor = mainPrefs.edit()
        val uuid = UUID.randomUUID().toString()
        editor.putString("installation_uuid", uuid)
        editor.apply()

        info("New UUID: $uuid")
    }
}
