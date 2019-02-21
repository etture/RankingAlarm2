package com.ydly.rankingalarm2.service

import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ydly.rankingalarm2.base.BaseService
import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

class DateChangeService : BaseService() {

    @Inject
    lateinit var alarmHistoryRepo: AlarmHistoryRepository

    private val compositeDisposable = CompositeDisposable()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val currentTimeInMillis: Long? = intent?.extras?.getLong("timeInMillis")
        info("onStartCommand() called, currentTime: $currentTimeInMillis")

        val calendar = Calendar.getInstance().apply { timeInMillis = currentTimeInMillis!! }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}