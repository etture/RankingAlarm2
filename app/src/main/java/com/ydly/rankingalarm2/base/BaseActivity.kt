package com.ydly.rankingalarm2.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ydly.rankingalarm2.ui.alarm.RingAlarmActivity
import org.jetbrains.anko.AnkoLogger

abstract class BaseActivity: AppCompatActivity(), AnkoLogger {

    private val injector = BaseApplication.getViewInjector()

    init {
        inject()
    }

    private fun inject() {
        when(this) {
            is RingAlarmActivity -> injector?.inject(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    abstract fun initialize()

}
