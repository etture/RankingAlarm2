package com.ydly.rankingalarm2.base

import android.app.AlarmManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ydly.rankingalarm2.receiver.DateChangeReceiver
import com.ydly.rankingalarm2.ui.alarm.AlarmFragment
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import javax.inject.Inject

abstract class BaseFragment: Fragment(), AnkoLogger {

    private val injector = BaseApplication.getViewInjector()

    @Inject
    lateinit var minuteTickReceiver: DateChangeReceiver

    @Inject
    lateinit var alarmManager: AlarmManager

    val subscription = CompositeDisposable()

    init {
        inject()
    }

    private fun inject() {
        when(this) {
            is AlarmFragment -> injector?.inject(this)
        }
    }

    abstract fun bind(inflater: LayoutInflater, container: ViewGroup?)

}
