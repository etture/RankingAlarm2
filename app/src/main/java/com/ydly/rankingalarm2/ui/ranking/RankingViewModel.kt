package com.ydly.rankingalarm2.ui.ranking

import android.content.SharedPreferences
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import javax.inject.Inject

class RankingViewModel: BaseViewModel() {

    @Inject
    lateinit var mainPrefs: SharedPreferences

    @Inject
    lateinit var alarmHistoryRepo: AlarmHistoryRepository

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