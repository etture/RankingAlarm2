package com.ydly.rankingalarm2.ui.alarm

import android.os.SystemClock
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

class RingAlarmViewModel: BaseViewModel() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    private val minute = MutableLiveData<String>().apply { this.value = "0" }
    private val second = MutableLiveData<String>().apply { this.value = "0" }
    private val millis = MutableLiveData<String>().apply { this.value = "0" }

    //========= Init and private functions (business logic) ==========

    init {

    }

    private fun untoggleAlarm(alarmData: AlarmData) {
        subscription += Flowable.fromCallable { alarmDataRepo.toggleChange(alarmData, false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun updateTime(elapsedTime: Long) {
        val baseSeconds = elapsedTime / 1000
        val baseMillis = elapsedTime % 1000

        val minuteDisplay = baseSeconds / 60
        val secondDisplay = baseSeconds % 60
        val millisDisplay = baseMillis / 10

        minute.value = if(minuteDisplay < 10) "0$minuteDisplay" else minuteDisplay.toString()
        second.value = if(secondDisplay< 10) "0$secondDisplay" else secondDisplay.toString()
        millis.value = if(millisDisplay < 10) "0$millisDisplay" else millisDisplay.toString()

//        info("elapsed: $elapsedTime, constructed: $minuteDisplay:$secondDisplay.$millisDisplay")
    }

    //========= Functions accessible by View (data manipulation) ==========

    fun untoggle(alarmData: AlarmData) {
        untoggleAlarm(alarmData)
    }

    fun updateTimeInMillis(elapsedTime: Long) {
        updateTime(elapsedTime)
    }

    fun clearSubscription() {
        subscription.clear()
    }

    //========= Functions accessible by View (DataBinding) ==========

    fun getMinute(): LiveData<String> = minute
    fun getSecond(): LiveData<String> = second
    fun getMillis(): LiveData<String> = millis

}
