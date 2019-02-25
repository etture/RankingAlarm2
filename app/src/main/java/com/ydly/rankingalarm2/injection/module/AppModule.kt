package com.ydly.rankingalarm2.injection.module

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ydly.rankingalarm2.receiver.DateChangeReceiver
import dagger.Module
import dagger.Provides

@Suppress("unused")
@Module
class AppModule(private val context: Context) {

    @Provides
    fun provideApplicationContext(): Context = context.applicationContext

    @Provides
    fun provideMainSharedPreferences(ctx: Context): SharedPreferences = ctx.getSharedPreferences("mainPrefs", Context.MODE_PRIVATE)

    @Provides
    fun provideAlarmManager(ctx: Context): AlarmManager  = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @Provides
    fun provideDateChangeReceiver(): DateChangeReceiver = DateChangeReceiver()

    @Provides
    fun provideConnectivityManager(ctx: Context): ConnectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

}
