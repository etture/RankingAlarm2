package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.service.TimeUpdateService
import org.jetbrains.anko.info
import java.util.*

class MinuteTickReceiver : BaseReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        if (action == Intent.ACTION_TIME_TICK) {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            info("Minute ticked, hour: $hour, minute: $minute")

            // 3 times when TimeUpdateService should be called
            // At 05:00, 11:00, and 00:00
            when {
                hour == 5 && minute == 0 -> {
                }
                hour == 11 && minute == 0 -> {
                }
                hour == 0 && minute == 0 -> {
                }
                else -> {
                    /* Do nothing */
                }
            }

            val timeUpdateIntent = Intent(context, TimeUpdateService::class.java)
            timeUpdateIntent.putExtra("timeInMillis", calendar.timeInMillis)
            context?.startService(timeUpdateIntent)
        }
    }

}