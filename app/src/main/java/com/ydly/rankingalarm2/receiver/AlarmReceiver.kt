package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import com.ydly.rankingalarm2.service.RingAlarmService
import com.ydly.rankingalarm2.ui.alarm.RingAlarmActivity
import com.ydly.rankingalarm2.util.ParcelableCreator
import com.ydly.rankingalarm2.util.ParcelableUtil
import org.jetbrains.anko.info

class AlarmReceiver: BaseReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        // Get Parcelable AlarmData as ByteArray and unmarshall it to get AlarmData
        val alarmDataByteArray = intent?.getByteArrayExtra("alarmDataByteArray")
        val alarmData = ParcelableUtil.unmarshall(alarmDataByteArray!!, ParcelableCreator.getAlarmDataCreator())

        info("onReceive() ->, alarmData2: $alarmData, intent: $intent, extras: ${intent.extras}")

        val serviceIntent = Intent(context!!, RingAlarmService::class.java)

        // Start RingAlarmService
        // Vary versions because Android O and beyond don't require startForegroundService()
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            context.startForegroundService(serviceIntent)
//        } else {
//            context.startService(serviceIntent)
//        }

        // What if I just start the Activity here?
        val activityIntent = Intent(context, RingAlarmActivity::class.java)
        activityIntent.putExtra("alarmData", alarmData)
        info("alarmData: $alarmData")
        activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(activityIntent)
    }

}