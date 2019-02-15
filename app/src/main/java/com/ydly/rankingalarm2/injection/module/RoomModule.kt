package com.ydly.rankingalarm2.injection.module

import android.arch.persistence.room.Room
import android.content.Context
import com.ydly.rankingalarm2.data.local.MIGRATION_1_2
import com.ydly.rankingalarm2.data.local.RankingAlarmDatabase
import com.ydly.rankingalarm2.data.local.alarm.AlarmDataDao
import com.ydly.rankingalarm2.injection.scope.RepositoryScope
import dagger.Module
import dagger.Provides

@Module(includes = [AppModule::class])
@Suppress("unused")
class RoomModule {

    @Provides
    @RepositoryScope
    fun provideRankingAlarmDatabase(context: Context): RankingAlarmDatabase {
        return Room.databaseBuilder(
            context,
            RankingAlarmDatabase::class.java,
            "rankingAlarm.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @RepositoryScope
    fun provideAlarmDataDao(rankingAlarmDatabase: RankingAlarmDatabase): AlarmDataDao {
        return rankingAlarmDatabase.alarmDataDao()
    }

}
