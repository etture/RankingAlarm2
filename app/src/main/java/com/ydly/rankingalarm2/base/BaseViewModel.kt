package com.ydly.rankingalarm2.base

import android.arch.lifecycle.ViewModel
import android.content.Intent
import com.ydly.rankingalarm2.ui.alarm.AlarmItemViewModel
import com.ydly.rankingalarm2.ui.alarm.AlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.CreateAlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.EditAlarmViewModel
import com.ydly.rankingalarm2.util.ResourceProvider
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import javax.inject.Inject

abstract class BaseViewModel : ViewModel(), AnkoLogger {

    private val injector = BaseApplication.getViewModelInjector()

    @Inject
    lateinit var res: ResourceProvider

    val subscription = CompositeDisposable()

    init {
        inject()
    }

    private fun inject() {
        when (this) {
            is AlarmViewModel -> injector?.inject(this)
            is AlarmItemViewModel -> injector?.inject(this)
            is CreateAlarmViewModel -> injector?.inject(this)
            is EditAlarmViewModel -> injector?.inject(this)
        }
    }

    open fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {}

}
