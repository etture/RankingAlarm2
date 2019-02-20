package com.ydly.rankingalarm2.injection.module

import com.ydly.rankingalarm2.data.repository.AlarmDataRepository
import com.ydly.rankingalarm2.data.repository.AlarmHistoryRepository
import com.ydly.rankingalarm2.injection.scope.ViewModelScope
import dagger.Module
import dagger.Provides

@Module
@Suppress("unused")
class RepositoryModule {

    @Provides
    fun provideAlarmDataRepository(): AlarmDataRepository = AlarmDataRepository()

    @Provides
    fun provideAlarmHistoryRepository(): AlarmHistoryRepository = AlarmHistoryRepository()

}
