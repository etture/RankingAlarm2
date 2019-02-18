package com.ydly.rankingalarm2.ui.alarm

import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
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

        red_btn.setOnTouchListener { view, motionEvent ->
            val redBtn = view as ImageView
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    redBtn.setImageResource(R.drawable.red_btn_pressed_medium)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    redBtn.setImageResource(R.drawable.red_btn_unpressed_medium)
                    true
                }
                else -> false
            }
        }
    }

}
