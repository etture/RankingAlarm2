package com.ydly.rankingalarm2.injection.component

import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.module.RepositoryModule
import com.ydly.rankingalarm2.injection.module.ResourceModule
import com.ydly.rankingalarm2.injection.scope.ViewModelScope
import com.ydly.rankingalarm2.ui.alarm.*
import com.ydly.rankingalarm2.ui.alarm.unused.AlarmItemViewModel
import com.ydly.rankingalarm2.ui.alarm.unused.AlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.unused.CreateAlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.unused.EditAlarmViewModel
import com.ydly.rankingalarm2.ui.ranking.RankingViewModel
import dagger.Component

@ViewModelScope
@Component(modules = [AppModule::class, RepositoryModule::class, ResourceModule::class], dependencies = [RepositoryInjector::class])
interface ViewModelInjector {

    fun inject(alarmViewModel: AlarmViewModel)
    fun inject(alarmItemViewModel: AlarmItemViewModel)
    fun inject(createAlarmViewModel: CreateAlarmViewModel)
    fun inject(editAlarmViewModel: EditAlarmViewModel)
    fun inject(singleAlarmViewModel: SingleAlarmViewModel)
    fun inject(ringAlarmViewModel: RingAlarmViewModel)

    fun inject(rankingViewModel: RankingViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun appModule(appModule: AppModule): Builder
        fun repositoryInjector(repositoryInjector: RepositoryInjector): Builder
    }

}
