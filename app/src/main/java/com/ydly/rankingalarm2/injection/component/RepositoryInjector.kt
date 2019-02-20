package com.ydly.rankingalarm2.injection.component

import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.injection.module.AppModule
import com.ydly.rankingalarm2.injection.module.RoomModule
import com.ydly.rankingalarm2.injection.scope.RepositoryScope
import dagger.Component

@RepositoryScope
@Component(modules = [AppModule::class, RoomModule::class])
interface RepositoryInjector {

    fun inject(alarmDataRepository: AlarmDataRepository)
    fun inject(alarmHistoryRepository: AlarmHistoryRepository)

    @Component.Builder
    interface Builder {
        fun build(): RepositoryInjector
        fun appModule(appModule: AppModule): Builder
    }

}
