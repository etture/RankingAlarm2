package com.ydly.rankingalarm2.injection.component

import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.module.RepositoryModule
import com.ydly.rankingalarm2.injection.module.ResourceModule
import com.ydly.rankingalarm2.injection.scope.ViewModelScope
import com.ydly.rankingalarm2.ui.alarm.AlarmItemViewModel
import com.ydly.rankingalarm2.ui.alarm.AlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.CreateAlarmViewModel
import com.ydly.rankingalarm2.ui.alarm.EditAlarmViewModel
import dagger.Component

@ViewModelScope
@Component(modules = [AppModule::class, RepositoryModule::class, ResourceModule::class], dependencies = [RepositoryInjector::class])
interface ViewModelInjector {

    fun inject(alarmViewModel: AlarmViewModel)
    fun inject(alarmItemViewModel: AlarmItemViewModel)
    fun inject(createAlarmViewModel: CreateAlarmViewModel)
    fun inject(editAlarmViewModel: EditAlarmViewModel)

    @Component.Builder
    interface Builder {
        fun build(): ViewModelInjector
        fun appModule(appModule: AppModule): Builder
        fun repositoryInjector(repositoryInjector: RepositoryInjector): Builder
    }

}
