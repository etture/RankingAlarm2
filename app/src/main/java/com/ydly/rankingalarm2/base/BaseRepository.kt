package com.ydly.rankingalarm2.base

import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import org.jetbrains.anko.AnkoLogger

abstract class BaseRepository: AnkoLogger {

    private val injector = BaseApplication.getRepositoryInjector()

    init {
        inject()
    }

    private fun inject() {
        when (this) {
            is AlarmDataRepository -> injector?.inject(this)
            is AlarmHistoryRepository -> injector?.inject(this)
        }
    }
}
