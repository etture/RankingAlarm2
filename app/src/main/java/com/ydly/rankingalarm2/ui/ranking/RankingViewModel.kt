package com.ydly.rankingalarm2.ui.ranking

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.util.SingleEvent
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.jetbrains.anko.info
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RankingViewModel: BaseViewModel() {

    @Inject
    lateinit var mainPrefs: SharedPreferences

    @Inject
    lateinit var alarmHistoryRepo: AlarmHistoryRepository

    private val refreshEvent = MutableLiveData<SingleEvent<Boolean>>()

    //========= Init and private functions (business logic) ==========



    //========= Functions accessible by View (data manipulation) ==========

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


    //========= Functions accessible by View (DataBinding) ==========

    fun observeRefreshEvent(): LiveData<SingleEvent<Boolean>> = refreshEvent

}