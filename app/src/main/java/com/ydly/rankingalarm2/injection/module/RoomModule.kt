package com.ydly.rankingalarm2.injection.module

import androidx.room.Room
import android.content.Context
import com.ydly.rankingalarm2.data.local.MIGRATION_1_2
import com.ydly.rankingalarm2.data.local.MIGRATION_2_3
import com.ydly.rankingalarm2.data.local.MIGRATION_3_4
import com.ydly.rankingalarm2.data.local.RankingAlarmDatabase
import com.ydly.rankingalarm2.data.local.alarm.AlarmDataDao
import com.ydly.rankingalarm2.data.local.alarm.AlarmHistoryDao
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
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4
            )
            .build()
    }

    @Provides
    @RepositoryScope
    fun provideAlarmDataDao(rankingAlarmDatabase: RankingAlarmDatabase): AlarmDataDao {
        return rankingAlarmDatabase.alarmDataDao()
    }

    @Provides
    @RepositoryScope
    fun provideAlarmHistoryDao(rankingAlarmDatabase: RankingAlarmDatabase): AlarmHistoryDao {
        return rankingAlarmDatabase.alarmHistoryDao()
    }

}
