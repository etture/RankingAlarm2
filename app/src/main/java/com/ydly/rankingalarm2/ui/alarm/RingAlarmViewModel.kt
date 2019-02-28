package com.ydly.rankingalarm2.ui.alarm

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.model.response.ErrorResponse
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.ConnectivityInterceptor
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
import com.ydly.rankingalarm2.util.PENDING_ALARM_HISTORY_JSON
import com.ydly.rankingalarm2.util.extension.fromJson
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RingAlarmViewModel : BaseViewModel() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    @Inject
    lateinit var alarmHistoryRepo: AlarmHistoryRepository

    @Inject
    lateinit var mainPrefs: SharedPreferences

    @Inject
    lateinit var gson: Gson

    private lateinit var alarmData: AlarmData

    private val minute = MutableLiveData<String>().apply { this.value = "0" }
    private val second = MutableLiveData<String>().apply { this.value = "0" }
    private val millis = MutableLiveData<String>().apply { this.value = "0" }

    //========= Init and private functions (business logic) ==========

    private fun untoggleAlarm() {
        subscription += Flowable.fromCallable { alarmDataRepo.toggleChange(alarmData, false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    // Update stopwatch time on View
    private fun updateTime(elapsedTime: Long) {
        val baseSeconds = elapsedTime / 1000
        val baseMillis = elapsedTime % 1000

        val minuteDisplay = baseSeconds / 60
        val secondDisplay = baseSeconds % 60
        val millisDisplay = baseMillis / 10

        minute.value = if (minuteDisplay < 10) "0$minuteDisplay" else minuteDisplay.toString()
        second.value = if (secondDisplay < 10) "0$secondDisplay" else secondDisplay.toString()
        millis.value = if (millisDisplay < 10) "0$millisDisplay" else millisDisplay.toString()
    }

    private fun insertAlarmHistory(wokeUp: Boolean, takenTimeInMillis: Long?) {

        val dateTimeUtil = DateTimeUtilMillisToUnits(alarmData.timeInMillis)
        val hour = dateTimeUtil.hour24
        val minute = dateTimeUtil.minute

        // Try to insert the alarmHistoryData into local DB
        // If it succeeds, then send it to the server as well
        // If not, then don't even bother with the server
        subscription += Flowable.fromCallable {
            alarmHistoryRepo.insertAlarmHistory(
                alarmTimeInMillis = alarmData.timeInMillis,
                wokeUp = wokeUp,
                takenTimeInMillis = takenTimeInMillis,
                hour = hour,
                minute = minute
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { insertId ->
                    info("insertAlarmHistory() -> insertId: $insertId")

                    // Get insertId from inserting into local DB
                    // insertId == 1 means alarm has already been stored locally for today
                    // Send info to SERVER if insertId is not -1
                    if (insertId > 0 && wokeUp) {

                        // Create alarmHistoryBody
                        val alarmHistoryBody = createAlarmHistoryBody(
                            idInDevice = insertId,
                            alarmTimeInMillis = alarmData.timeInMillis,
                            takenTimeInMillis = takenTimeInMillis!!,
                            hour = hour,
                            minute = minute
                        )

                        alarmHistoryRepo.uploadAlarmHistory(alarmHistoryBody)
                            // When there is no Internet or Server is down, retry 3 times, then handle error
                            .retryWhen { error ->
                                error.zipWith(Flowable.range(1, 4)) { err: Throwable, cnt: Int -> Pair(err, cnt) }
                                    .flatMap { (throwable, count) ->
                                        info("uploadAlarmHistory() -> retryWhen -> error: $throwable")
                                        if (count < 4) {
                                            when (throwable) {
                                                is ConnectivityInterceptor.OfflineException -> {
                                                    Flowable.timer(4, TimeUnit.SECONDS)
                                                }
                                                is SocketTimeoutException -> {
                                                    Flowable.timer(1, TimeUnit.SECONDS)
                                                }
                                                else -> {
                                                    Flowable.error(throwable)
                                                }
                                            }
                                        } else {
                                            Flowable.error(throwable)
                                        }
                                    }
                            }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                onNext = { response ->
                                    if (response.isSuccessful) {

                                        val message = response.body()?.message
                                        val originalId = response.body()?.originalId
                                        val dayRank = response.body()?.dayRank
                                        val morningRank = response.body()?.morningRank
                                        info("uploadAlarmHistory() -> message: $message, originalId: $originalId, dayRank: $dayRank, morningRank: $morningRank")
                                        // Update alarmHistory with ranks
                                        Flowable.fromCallable {
                                            alarmHistoryRepo.updateRank(
                                                originalId!!,
                                                dayRank!!,
                                                morningRank!!
                                            )
                                        }
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribeBy(
                                                onNext = {
                                                    info("updateRank() -> Ranks updated properly")
                                                },
                                                onError = { error ->
                                                    info("updateRank() -> error: $error")
                                                }
                                            )

                                    } else {

                                        val statusCode = response.code()
                                        val jsonErrorObj = JSONObject(response.errorBody()?.string())
                                        val mError: ErrorResponse =
                                            gson.fromJson(jsonErrorObj.toString(), ErrorResponse::class.java)

                                        info(
                                            "uploadAlarmHistory() -> statusCode: $statusCode, mError.message: ${mError.message}"
                                        )

                                        // Handle CONNECTION ERROR here, possibly by saving the alarmHistoryData stuff
                                        // somewhere so it can be later sent to the server again when connection is reestablished
                                        when (statusCode) {
                                            409 -> {
                                                info("uploadAlarmHistory() -> duplicate in server, no action done")
                                            }
                                            else -> {
                                                putHistoryToPrefs(alarmHistoryBody)
                                                info("uploadAlarmHistory() -> error other than duplicate, pending history saved")
                                            }
                                        }

                                    }
                                },
                                onError = { error ->
                                    info("uploadAlarmHistory() -> error: $error")

                                    // In case of no-internet exception or when server is down
                                    putHistoryToPrefs(alarmHistoryBody)
                                }
                            )
                    }

                },
                onError = { error ->
                    info("insertAlarmHistory() -> error: $error")
                    // Action when insertion into local DB causes an error
                }
            )
    }

    private fun createAlarmHistoryBody(
        idInDevice: Long,
        alarmTimeInMillis: Long,
        takenTimeInMillis: Long,
        hour: Int,
        minute: Int
    ): AlarmHistoryBody {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = alarmTimeInMillis
        info("createAlarmHistoryBody() -> alarmTimeInMillis: $alarmTimeInMillis")

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val baseTimeInMillis = calendar.timeInMillis
        val timeZoneId = calendar.timeZone.id

        info("createAlarmHistoryBody() -> baseTimeInMillis: $baseTimeInMillis")

        val uuid: String = mainPrefs.getString("installation_uuid", null)!!
        return AlarmHistoryBody(
            idInDevice = idInDevice,
            userUUID = uuid,
            year = year,
            month = month,
            dayOfMonth = dayOfMonth,
            hour = hour,
            minute = minute,
            timeZoneId = timeZoneId,
            baseTimeInMillis = baseTimeInMillis,
            alarmTimeInMillis = alarmTimeInMillis,
            takenTimeInMillis = takenTimeInMillis
        )
    }

    // Function to put JSON array (in String) of the pending alarmHistoryBody objects into mainPrefs
    // If the mainPrefs is empty, get [] and insert the new pending history there
    // If the mainPrefs already has some history items, then add to that list and save as JSON array
    private fun putHistoryToPrefs(newHistoryBody: AlarmHistoryBody) {
        val historyJsonArray = mainPrefs.getString(PENDING_ALARM_HISTORY_JSON, "[]")
        info("historyJsonArray: $historyJsonArray")
        val historyList: MutableList<AlarmHistoryBody> =
            gson.fromJson<MutableList<AlarmHistoryBody>>(historyJsonArray!!)

        historyList.add(newHistoryBody)
        val historyListJson = gson.toJsonTree(historyList)

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
    }

    //========= Functions accessible by View (data manipulation) ==========

    // Called once in Activity's onCreate() -> initialize()
    // Set original alarmData and untoggle it via DB
    fun registerAlarmData(alarmData: AlarmData) {
        this.alarmData = alarmData
        untoggleAlarm()
    }

    // Called by View every 10ms for stopwatch functionality
    fun updateTimeInMillis(elapsedTime: Long) {
        updateTime(elapsedTime)
    }

    // When stopwatch is stopped, whether by proper alarm-off action / snooze / improper turn-off
    // Set new alarm history marking time and wokeUp status
    fun setNewAlarmHistory(wokeUp: Boolean, takenTimeInMillis: Long? = null) {
        insertAlarmHistory(wokeUp, takenTimeInMillis)
    }

    fun clearSubscription() {
        subscription.clear()
    }

    //========= Functions accessible by View (DataBinding) ==========

    fun getMinute(): LiveData<String> = minute
    fun getSecond(): LiveData<String> = second
    fun getMillis(): LiveData<String> = millis

}
