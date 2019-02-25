package com.ydly.rankingalarm2.ui.alarm

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.remote.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.ErrorResponse
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.ConnectivityInterceptor
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
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
                    if (insertId > -2) {

                        // TODO Create alarmHistoryBody
                        val alarmHistoryBody = createAlarmHistoryBody(
                            alarmTimeInMillis = alarmData.timeInMillis,
                            wokeUp = wokeUp,
                            takenTimeInMillis = takenTimeInMillis,
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
                                        info("uploadAlarmHistory() -> message: $message")
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
                                                val alarmHistoryJson = gson.toJson(alarmHistoryBody)
                                                putToPrefs("pendingAlarmHistoryJSON", alarmHistoryJson)
                                            }
                                        }
                                    }
                                },
                                onError = { error ->
                                    info("uploadAlarmHistory() -> error: $error")

                                    val alarmHistoryJson = gson.toJson(alarmHistoryBody)
                                    putToPrefs("pendingAlarmHistoryJSON", alarmHistoryJson)
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
        alarmTimeInMillis: Long,
        takenTimeInMillis: Long?,
        wokeUp: Boolean,
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
            userUUID = uuid,
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
    }

    private fun putToPrefs(key: String, value: String) {
        val editor = mainPrefs.edit()
        editor.putString(key, value)
        editor.apply()
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
