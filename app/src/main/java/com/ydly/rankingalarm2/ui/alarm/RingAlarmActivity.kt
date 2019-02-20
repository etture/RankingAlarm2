package com.ydly.rankingalarm2.ui.alarm

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import com.ydly.rankingalarm2.data.local.alarm.AlarmData
import kotlinx.android.synthetic.main.activity_ring_alarm.*
import org.jetbrains.anko.info

class RingAlarmActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(RingAlarmViewModel::class.java) }
    private lateinit var binding: com.ydly.rankingalarm2.databinding.ActivityRingAlarmBinding

    override fun initialize() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ring_alarm)
        binding.viewModel = viewModel

        val message = intent.getStringExtra("message")
        ringAlarm_txtVw_text.text = message
        val alarmData = intent.getParcelableExtra<AlarmData>("alarmData")
        viewModel.untoggle(alarmData)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.redBtn.setOnTouchListener { view, motionEvent ->
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

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearSubscription()
    }
}
