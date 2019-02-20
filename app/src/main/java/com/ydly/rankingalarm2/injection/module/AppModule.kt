package com.ydly.rankingalarm2.injection.module

import android.app.AlarmManager
import android.content.Context
import com.ydly.rankingalarm2.receiver.DateChangeReceiver
import dagger.Module
import dagger.Provides

@Module
@Suppress("unused")
class AppModule(private val context: Context) {

    @Provides
    fun provideApplicationContext(): Context = context.applicationContext

    @Provides
    fun provideAlarmManager(ctx: Context): AlarmManager  = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideDateChangeReceiver(): DateChangeReceiver = DateChangeReceiver()

}
