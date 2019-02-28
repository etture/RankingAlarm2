package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import android.os.Build
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.service.PastAlarmRingingService
import org.jetbrains.anko.info
import java.util.*

class PastAlarmReceiver : BaseReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        ctx = context!!

        // String delivered from Intent
        val stateString = intent?.extras?.getString("state")

        // Generate Intent for PastAlarmRingingService
        val serviceIntent = Intent(ctx, PastAlarmRingingService::class.java)

        // Send extra String value to RingtonePlayingService
        serviceIntent.putExtra("state", stateString)

        val cal = Calendar.getInstance()
        if(stateString == "alarm on") info("Alarm went off at ${cal.time}")

        // Start the ringtone Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(serviceIntent)
        } else {
            ctx.startService(serviceIntent)
        }
    }

}
