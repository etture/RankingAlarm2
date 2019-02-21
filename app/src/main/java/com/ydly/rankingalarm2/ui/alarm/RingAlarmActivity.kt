package com.ydly.rankingalarm2.ui.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import com.ydly.rankingalarm2.data.local.alarm.AlarmData

class RingAlarmActivity : BaseActivity() {

    private val viewModel by lazy { ViewModelProviders.of(this).get(RingAlarmViewModel::class.java) }
    private lateinit var binding: com.ydly.rankingalarm2.databinding.ActivityRingAlarmBinding

    // Needed for acquiring WakeLock when waking from lockscreen
    private val pm by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val wl by lazy {
        pm.newWakeLock(
        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
        this.loggerTag)
    }

    private lateinit var screenStatusReceiver: BroadcastReceiver

    private val startTime = System.currentTimeMillis()
    private val handler = Handler()
    private val stopwatch = object : Runnable {
        override fun run() {
            val elapsedTime = System.currentTimeMillis() - startTime
            viewModel.updateTimeInMillis(elapsedTime)
            handler.postDelayed(this, 10)
        }
    }

    override fun initialize() {
        this.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ring_alarm)
        binding.viewModel = viewModel

        val alarmData = intent.getParcelableExtra<AlarmData>("alarmData")
        viewModel.untoggle(alarmData)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire WakeLock for 5 minutes max
        wl.acquire(300000)

        binding.redBtn.setOnTouchListener { view, motionEvent ->
            val redBtn = view as ImageView
            when(motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    redBtn.setImageResource(R.drawable.red_btn_pressed_medium)
                    handler.removeCallbacks(stopwatch)
                    releaseWakeLock()
                    finish()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    redBtn.setImageResource(R.drawable.red_btn_unpressed_medium)
                    true
                }
                else -> false
            }
        }

        // Receiver to finish Activity when screen is turned off
        screenStatusReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.action
                when(action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        releaseWakeLock()
                        finish()
                    }
                }
            }
        }
        registerReceiver(screenStatusReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        handler.post(stopwatch)

    }

    override fun onBackPressed() {
        super.onBackPressed()

        releaseWakeLock()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.clearSubscription()
        handler.removeCallbacks(stopwatch)
        unregisterReceiver(screenStatusReceiver)

        releaseWakeLock()
    }

    private fun releaseWakeLock() {
        // Release WakeLock
        if(wl.isHeld) wl.release()
    }
}
