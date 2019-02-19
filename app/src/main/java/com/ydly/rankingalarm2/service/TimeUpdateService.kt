package com.ydly.rankingalarm2.service

import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ydly.rankingalarm2.base.BaseService
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import javax.inject.Inject

class TimeUpdateService : BaseService() {

    @Inject
    lateinit var alarmDataRepo: AlarmDataRepository

    private val compositeDisposable = CompositeDisposable()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentTimeInMillis: Long? = intent?.extras?.getLong("timeInMillis")
        info("onStartCommand() called, currentTime: $currentTimeInMillis")

//        inspectAlarmListForTimePassed(currentTimeInMillis!!)

        return START_NOT_STICKY
    }

    private fun inspectAlarmListForTimePassed(currentTimeInMillis: Long) {
        info("inspectAlarmListForTimePassed() called")
        compositeDisposable += alarmDataRepo.getAlarms()
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onNext = { dbAlarmList ->
                    val ids: MutableList<Long> = mutableListOf()

                    // Iterate through the whole alarmList and see if any dates should be set one day later
                    // If so, add the ids of the alarmItems to be delivered to Repository
                    for (alarmItem in dbAlarmList) {
                        if (alarmItem.timeInMillis <= currentTimeInMillis) {
                            ids.add(alarmItem.id!!)
                        }
                    }
                    info("inspectAlarmListForTimePassed() ids: $ids")

                    // Deliver the list of ids to the Repository to update DB
                    // and send broadcast back to Activity/Fragment to update the UI
                    if (ids.size > 0) {
                        alarmDataRepo.updateDatesToNextDay(ids)

                        val datesUpdatedIntent = Intent("datesUpdated")
                        datesUpdatedIntent.putExtra("datesUpdated", true)
                        LocalBroadcastManager.getInstance(this).sendBroadcast(datesUpdatedIntent)
                    }

                },
                onError = { error ->
                    info("inspectAlarmListForTimePassed() subscribe error, $error")
                }
            )
    }
}