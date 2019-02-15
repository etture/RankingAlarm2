package com.ydly.rankingalarm2.ui.alarm

import android.os.Bundle
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import kotlinx.android.synthetic.main.activity_ring_alarm.*
import org.jetbrains.anko.info

class RingAlarmActivity : BaseActivity() {

    override fun bind() {
        info("activity started")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ring_alarm)

        val message = intent.getStringExtra("message")
        ringAlarm_txtVw_text.text = message
    }

}
