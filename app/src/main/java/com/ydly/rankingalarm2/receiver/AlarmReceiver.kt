package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.service.RingAlarmService
import com.ydly.rankingalarm2.ui.alarm.RingAlarmActivity
import org.jetbrains.anko.info

class AlarmReceiver: BaseReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val message = intent?.getStringExtra("message")
        val alarmData = intent?.getParcelableExtra<AlarmData>("alarmData")
        info("onReceive() -> message: $message")

        val serviceIntent = Intent(context!!, RingAlarmService::class.java)
        serviceIntent.putExtra("message", message)

        // Start RingAlarmService
        // Vary versions because Android O and beyond don't require startForegroundService()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(serviceIntent)
//        } else {
//            context.startService(serviceIntent)
//        }

        // What if I just start the Activity here?
        val activityIntent = Intent(context, RingAlarmActivity::class.java)
        activityIntent.putExtra("message", message)
        activityIntent.putExtra("alarmData", alarmData)
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(activityIntent)
    }

}