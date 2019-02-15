package com.ydly.rankingalarm2.base

import android.app.Service
import com.ydly.rankingalarm2.service.TimeUpdateService
import com.ydly.rankingalarm2.util.ResourceProvider
import org.jetbrains.anko.AnkoLogger
import javax.inject.Inject

abstract class BaseService : Service(), AnkoLogger {

    private val injector = BaseApplication.getServiceInjector()

    @Inject
    lateinit var res: ResourceProvider

    init {
        inject()
    }

    private fun inject() {
        when (this) {
            is TimeUpdateService -> injector?.inject(this)
        }
    }

}