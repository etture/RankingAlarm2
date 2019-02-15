package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.service.TimeUpdateService
import org.jetbrains.anko.info
import java.util.*

class MinuteTickReceiver: BaseReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        if(action == Intent.ACTION_TIME_TICK) {
            info("Minute ticked: ${calendar.get(Calendar.MINUTE)}")
            val timeUpdateIntent = Intent(context, TimeUpdateService::class.java)
            timeUpdateIntent.putExtra("timeInMillis", calendar.timeInMillis)
            context?.startService(timeUpdateIntent)
        }
    }

}