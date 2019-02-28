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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.util.ACTION_ALARM_TURNED_OFF
import com.ydly.rankingalarm2.util.SLEPT_IN
import com.ydly.rankingalarm2.util.WOKE_UP
import javax.inject.Inject

class RingAlarmActivity : BaseActivity() {

    @Inject
    lateinit var localBroadcastManager: LocalBroadcastManager

    private val viewModel by lazy { ViewModelProviders.of(this).get(RingAlarmViewModel::class.java) }
    private lateinit var binding: com.ydly.rankingalarm2.databinding.ActivityRingAlarmBinding

    // Needed for acquiring WakeLock when waking from lockscreen
    private val pm by lazy { getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val wl by lazy {
        pm.newWakeLock(
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            this.loggerTag
        )
    }

    // Receiver for getting screen-off action
    private lateinit var screenStatusReceiver: BroadcastReceiver

    // Stopwatch to start as soon as Activity launches
    private val startTime: Long = System.currentTimeMillis()
    private var elapsedTime: Long = 0L
    private val handler = Handler()
    private val stopwatch = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime
            viewModel.updateTimeInMillis(elapsedTime)

            // Finish Activity if 5 minutes have passed
            if (elapsedTime > 300000) {
                viewModel.setNewAlarmHistory(SLEPT_IN)
                finishAndSignalFinished()
            }

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
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        binding = DataBindingUtil.setContentView(this, R.layout.activity_ring_alarm)
        binding.viewModel = viewModel

        val alarmData = intent.getParcelableExtra<AlarmData>("alarmData")

        // Register alarmData to ViewModel and untoggle the alarm via DB
        viewModel.registerAlarmData(alarmData)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Acquire WakeLock for 5 minutes max -> after 5 mins, automatically releases WakeLock
        wl.acquire(300000)

        binding.redBtn.setOnTouchListener { view, motionEvent ->
            val redBtn = view as ImageView
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    redBtn.setImageResource(R.drawable.red_btn_pressed_medium)
                    handler.removeCallbacks(stopwatch)

                    // Insert new AlarmHistoryData with elapsedTime and WOKE_UP status
                    viewModel.setNewAlarmHistory(WOKE_UP, elapsedTime)

                    finishAndSignalFinished()
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
                when (action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        // Insert new AlarmHistoryData with elapsedTime and SLEPT_IN status
                        viewModel.setNewAlarmHistory(SLEPT_IN)

                        finishAndSignalFinished()
                    }
                }
            }
        }
        registerReceiver(screenStatusReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        handler.post(stopwatch)

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // Insert new AlarmHistoryData with elapsedTime and SLEPT_IN status
        viewModel.setNewAlarmHistory(SLEPT_IN)

        finishAndSignalFinished()
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
        if (wl.isHeld) wl.release()
    }

    private fun finishAndSignalFinished() {
        releaseWakeLock()
        localBroadcastManager.sendBroadcast(Intent(ACTION_ALARM_TURNED_OFF))
        finish()
    }
}
