package com.ydly.rankingalarm2.injection.module

import androidx.room.Room
import android.content.Context
import com.ydly.rankingalarm2.data.local.*
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmDataDao
import com.ydly.rankingalarm2.data.local.alarm.dao.AlarmHistoryDao
import com.ydly.rankingalarm2.injection.scope.RepositoryScope
import dagger.Module
import dagger.Provides

@Suppress("unused")
@Module(includes = [AppModule::class])
class RoomModule {

    @RepositoryScope
    @Provides
    fun provideRankingAlarmDatabase(context: Context): RankingAlarmDatabase {
        return Room.databaseBuilder(
            context,
            RankingAlarmDatabase::class.java,
            "rankingAlarm.db"
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7
            )
            .build()
    }

    @RepositoryScope
    @Provides
    fun provideAlarmDataDao(rankingAlarmDatabase: RankingAlarmDatabase): AlarmDataDao {
        return rankingAlarmDatabase.alarmDataDao()
    }

    @RepositoryScope
    @Provides
    fun provideAlarmHistoryDao(rankingAlarmDatabase: RankingAlarmDatabase): AlarmHistoryDao {
        return rankingAlarmDatabase.alarmHistoryDao()
    }

}
