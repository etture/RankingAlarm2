package com.ydly.rankingalarm2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ydly.rankingalarm2.R
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.toast

const val START = 1
const val STOP = 0

class PastAlarmRingingService : Service(), AnkoLogger {

    lateinit var mediaPlayer: MediaPlayer
    var startId: Int = 0
    var isRunning: Boolean = false


    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
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
        val getState: String? = intent?.extras?.getString("state")

        when (getState) {
            "alarm on" -> this.startId = START
            "alarm off" -> this.startId = STOP
            else -> this.startId = 0
        }

        // Alarm sound X, clicked alarm startBtn
        if (!this.isRunning && this.startId == START) {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio)
            mediaPlayer.start()

            
            this.isRunning = true
            this.startId = STOP

            toast("알람 X, 시작 버튼 누름")
        }
        // Alarm sound O, clicked alarm stopBtn
        else if (this.isRunning && this.startId == STOP) {
            // Turn off sound
            mediaPlayer.apply {
                stop()
                reset()
                release()
            }

            this.isRunning = false
            this.startId = STOP

            stopForeground(true)

            toast("알람 O, 스탑 버튼 누름")
        }
        // Alarm sound X, clicked alarm stopBtn
        else if (!this.isRunning && this.startId == STOP) {
            this.isRunning = false
            this.startId = STOP

            stopForeground(true)

            toast("알람 X, 스탑 버튼 누름")
        }
        // Alarm sound O, clicked alarm startBtn
        else if (this.isRunning && this.startId == START) {
            this.isRunning = true
            this.startId = START

            toast("알람 O, 시작 버튼 누름")
        } else { toast("위 옵션들이 아님") }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopForeground(true)
        debug("서비스 파괴")
    }
}
