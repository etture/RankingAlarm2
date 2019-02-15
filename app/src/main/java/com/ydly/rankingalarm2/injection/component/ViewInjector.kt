package com.ydly.rankingalarm2.injection.component

import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.scope.ViewScope
import com.ydly.rankingalarm2.ui.alarm.AlarmFragment
import dagger.Component

@ViewScope
@Component(modules = [AppModule::class])
interface ViewInjector {

    fun inject(alarmFragment: AlarmFragment)

    @Component.Builder
    interface Builder {
        fun build(): ViewInjector
        fun appModule(appModule: AppModule): Builder
    }

}