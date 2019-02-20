package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import com.ydly.rankingalarm2.base.BaseReceiver
import com.ydly.rankingalarm2.service.DateChangeService
import org.jetbrains.anko.info
import java.util.*

class DateChangeReceiver : BaseReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MILLISECOND, 0)
        if (action == Intent.ACTION_TIME_CHANGED) {
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            info("Date Changed, month: $month, day: $day")

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            if(hour == 0 && minute == 0) {
                val newDayIntent = Intent(context, DateChangeService::class.java)
                newDayIntent.putExtra("timeInMillis", calendar.timeInMillis)
                context?.startService(newDayIntent)
            }

        }
    }

}