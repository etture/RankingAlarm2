package com.ydly.rankingalarm2.injection.component

import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.scope.ViewScope
import com.ydly.rankingalarm2.ui.alarm.RingAlarmActivity
import com.ydly.rankingalarm2.ui.alarm.unused.AlarmFragment
import com.ydly.rankingalarm2.ui.alarm.SingleAlarmFragment
import com.ydly.rankingalarm2.ui.ranking.RankingFragment
import dagger.Component

@ViewScope
@Component(modules = [AppModule::class])
interface ViewInjector {

    fun inject(alarmFragment: AlarmFragment)
    fun inject(singleAlarmFragment: SingleAlarmFragment)

    fun inject(ringAlarmActivity: RingAlarmActivity)

    fun inject(rankingFragment: RankingFragment)

    @Component.Builder
    interface Builder {
        fun build(): ViewInjector
        fun appModule(appModule: AppModule): Builder
    }

}