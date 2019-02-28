package com.ydly.rankingalarm2.receiver

import android.content.Context
import android.content.Intent
import com.ydly.rankingalarm2.base.BaseReceiver
import org.jetbrains.anko.info
import java.util.*

class MinuteTickReceiver: BaseReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        info("minuteTickReceiver -> onReceive()")
        if (action == Intent.ACTION_TIME_TICK) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            info("minuteTickReceiver -> minute ticked -> hour: $hour, minute: $minute")
        }
    }
}
