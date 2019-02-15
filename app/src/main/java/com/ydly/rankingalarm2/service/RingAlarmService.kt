package com.ydly.rankingalarm2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseService
import com.ydly.rankingalarm2.ui.alarm.RingAlarmActivity

class RingAlarmService : BaseService() {

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val CHANNEL_ID = "default"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)

            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("알람 시작")
                .setContentText("알람음이 재생됩니다")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()

            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val message = intent?.getStringExtra("message")

        val activityIntent = Intent(this, RingAlarmActivity::class.java)

        activityIntent.putExtra("message", message)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(activityIntent)


        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
    }
}
