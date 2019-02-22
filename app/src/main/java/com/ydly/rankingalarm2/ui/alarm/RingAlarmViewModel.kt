package com.ydly.rankingalarm2.ui.alarm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.ConnectivityInterceptor
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.zipWith
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RingAlarmViewModel : BaseViewModel() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    @Inject
    lateinit var alarmHistoryRepo: AlarmHistoryRepository

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
        // Try to insert the alarmHistoryData into local DB
        // If it succeeds, then send it to the server as well
        // If not, then don't even bother with the server
        subscription += Flowable.fromCallable {
            alarmHistoryRepo.insertAlarmHistory(
                alarmTimeInMillis = alarmData.timeInMillis,
                wokeUp = wokeUp,
                takenTimeInMillis = takenTimeInMillis
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            // When there is no Internet or Server is down, retry 3 times, then handle error
            .retryWhen { error ->
                error.zipWith(Flowable.range(1, 3)) { err: Throwable, cnt: Int -> Pair(err, cnt) }
                    .flatMap { (throwable, count) ->
                        info("onClickToggle() -> retryWhen -> error: $throwable")
                        if (count < 3) {
                            when (throwable) {
                                is ConnectivityInterceptor.OfflineException -> {
                                    Flowable.timer(3, TimeUnit.SECONDS)
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
            .subscribeBy(
                onNext = { insertId ->
                    info("insertAlarmHistory() -> insertId: $insertId")

                },
                onError = { error ->
                    info("insertAlarmHistory() -> error: $error")
                    // TODO handle CONNECTION ERROR here, possibly by saving the alarmHistoryData stuff
                    // somewhere so it can be later sent to the server again when connection is reestablished
                }
            )
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
