package com.ydly.rankingalarm2.base

import android.app.AlarmManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ydly.rankingalarm2.receiver.DateChangeReceiver
import com.ydly.rankingalarm2.ui.alarm.unused.AlarmFragment
import com.ydly.rankingalarm2.ui.alarm.SingleAlarmFragment
import com.ydly.rankingalarm2.ui.ranking.RankingFragment
import io.reactivex.disposables.CompositeDisposable
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import javax.inject.Inject

abstract class BaseFragment: Fragment(), AnkoLogger {

    private val injector = BaseApplication.getViewInjector()

    @Inject
    lateinit var alarmManager: AlarmManager

    val subscription = CompositeDisposable()

    init {
        inject()
        info("initialize -> alarmManager: $alarmManager")
    }

    private fun inject() {
        when(this) {
            is AlarmFragment -> injector?.inject(this)
            is SingleAlarmFragment -> injector?.inject(this)
            is RankingFragment -> injector?.inject(this)
        }
    }

    abstract fun initialize(inflater: LayoutInflater, container: ViewGroup?)

}
