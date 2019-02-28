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

    // MediatorLiveData for 2-way DataBinding on refreshEvent
//    private val refreshingMediator = MediatorLiveData<Boolean>().apply {
//        addSource(refreshEvent) { value ->
//            setValue(value)
//            info("refreshEvent status changed: ${refreshEvent.value}")
//        }
//    }.also { it.observeForever { /*Do nothing*/ } }

    fun swipeTest() {
        info("swipeTest()")
        subscription += Flowable.timer(1, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    info("delayed 1 sec")
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

    fun observeRefreshEvent(): LiveData<SingleEvent<Boolean>> = refreshEvent

}