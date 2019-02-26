package com.ydly.rankingalarm2.ui.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.AlarmManagerCompat
import com.google.android.gms.ads.AdRequest
import com.ydly.rankingalarm2.BuildConfig
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.receiver.AlarmReceiver
import com.ydly.rankingalarm2.util.ParcelableUtil
import com.ydly.rankingalarm2.util.SingleEvent
import org.jetbrains.anko.info

class SingleAlarmFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = SingleAlarmFragment()
    }

    private val viewModel: SingleAlarmViewModel by lazy { ViewModelProviders.of(activity!!).get(SingleAlarmViewModel::class.java) }
    private lateinit var binding: com.ydly.rankingalarm2.databinding.FragmentSingleAlarmBinding

    private lateinit var newToastObserver: Observer<SingleEvent<String>>
    private lateinit var initialToggleSetEventObserver: Observer<SingleEvent<Triple<Boolean, Int, Int>>>
    private lateinit var toggleBackOffEventObserver: Observer<SingleEvent<Boolean>>

    private lateinit var activateEventObserver: Observer<SingleEvent<AlarmData>>
    private lateinit var deactivateEventObserver: Observer<SingleEvent<AlarmData>>

    override fun initialize(inflater: LayoutInflater, container: ViewGroup?) {
        // ViewModel and DataBinding setup
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_single_alarm, container, false)
        binding.viewModel = viewModel

        val adRequestBuilder = AdRequest.Builder()
        if(BuildConfig.DEBUG) {
            adRequestBuilder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        }
        val adRequest = adRequestBuilder.build()

        binding.singleAlarmFragAdView.loadAd(adRequest)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        initialize(inflater, container)
        info("onCreateView()")

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        info("onResume()")
        viewModel.updateDate()

        // Observe function in ViewModel to get a new Toast message and display it
        newToastObserver = Observer {
            it?.getContentIfNotHandled()?.let { toastText ->
                info("observeNewToast() -> message: $toastText")
                val toast = Toast.makeText(activity, toastText, Toast.LENGTH_SHORT)
                val v: TextView? = toast.view.findViewById(android.R.id.message)
                v?.gravity = Gravity.CENTER_HORIZONTAL
                toast.show()
            }
        }

        // Observe function in ViewModel to set initial state of SingleAlarmFragment
        // If alarm from DB is activated, then set ToggleButton to checked
        // If alarm from DB is deactivated, then set ToggleButton to unchecked
        initialToggleSetEventObserver = Observer {
            it?.getContentIfNotHandled()?.let { (initialToggledOn, initialHour, initialMinute) ->
                info("observeInitialToggleSetEvent() -> initialToggledOn: $initialToggledOn")
                binding.toggleButton.isChecked = initialToggledOn
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.singleAlarmFragTimePicker.hour = initialHour
                    binding.singleAlarmFragTimePicker.minute = initialMinute
                } else {
                    binding.singleAlarmFragTimePicker.currentHour = initialHour
                    binding.singleAlarmFragTimePicker.currentMinute = initialMinute
                }
            }
        }

        // Observe function in ViewModel to set the ToggleButton back off
        // For situations where the time of the attempted alarm is out of the accepted range (05:00 ~ 10:59)
        toggleBackOffEventObserver = Observer {
            it?.getContentIfNotHandled()?.let { toggleBackOff ->
                info("observeToggleBackOffEvent() -> toggleBackOff: $toggleBackOff")
                if (!toggleBackOff) {
                    binding.toggleButton.isChecked = toggleBackOff
                }
            }
        }

        // Observe function in ViewModel to get whether alarm was activated and set the alarm
        activateEventObserver = Observer {
            it?.getContentIfNotHandled()?.let { alarmData ->
                info("activateEventObserver -> $alarmData")

                val requestCode = (alarmData.timeInMillis / 1000).toInt()
                val alarmTime = alarmData.timeInMillis

                val activateAlarmIntent = Intent(activity, AlarmReceiver::class.java)

                // marshall AlarmData into ByteArray to be serialized properly
                activateAlarmIntent.putExtra("alarmDataByteArray", ParcelableUtil.marshall(alarmData))

                info("alarmTime: $alarmTime, requestCode: $requestCode")

                // PendingIntent to send broadcast to AlarmReceiver at specified time
                // Use timeInMillis as 2nd parameter (requestCode),
                // which uniquely identifies this PendingIntent
                val pendingIntent = PendingIntent.getBroadcast(
                    activity,
                    requestCode,
                    activateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )


                info("pendingIntent: $pendingIntent, alarmData: $alarmData")

                // Set AlarmManager with PendingIntent at the specified time
                // For exact time-setting, vary functions based on API version
                // AlarmManagerCompat takes care of API versions
                AlarmManagerCompat.setExactAndAllowWhileIdle(
                    alarmManager,
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            }
        }

        // Observe function in ViewModel to get whether alarm was deactivated and cancel the alarm
        deactivateEventObserver = Observer {
            it?.getContentIfNotHandled()?.let { alarmData ->
                info("deactivateEventObserver -> $alarmData")

                val requestCode = (alarmData.timeInMillis / 1000).toInt()

                val deactivateAlarmIntent = Intent(activity, AlarmReceiver::class.java)
                deactivateAlarmIntent.putExtra("message", alarmData.toString())

                // PendingIntent to send broadcast to AlarmReceiver at specified time
                // Use alarmData.id as 2nd parameter (requestCode),
                // which uniquely identifies this PendingIntent
                val pendingIntent = PendingIntent.getBroadcast(
                    activity,
                    requestCode,
                    deactivateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Both PendingIntent and AlarmManager must be cancelled to completely deactivate the alarm
                pendingIntent.cancel()
                alarmManager.cancel(pendingIntent)
            }
        }

        // Calls to ViewModel functions
        viewModel.also {

            // Update dateString in case the time has changed and should be reflected in the date
            it.updateDate()

            // Observe Observers
            it.observeNewToast().observe(activity!!, newToastObserver)
            it.observeInitialToggleSetEvent().observe(activity!!, initialToggleSetEventObserver)
            it.observeToggleBackOffEvent().observe(activity!!, toggleBackOffEventObserver)
            it.observeActivateEvent().observe(activity!!, activateEventObserver)
            it.observeDeactivateEvent().observe(activity!!, deactivateEventObserver)

        }
    }

    override fun onPause() {
        super.onPause()
        info("onPause()")

        viewModel.apply {
            // Remove Observers
            observeNewToast().removeObserver(newToastObserver)
            observeInitialToggleSetEvent().removeObserver(initialToggleSetEventObserver)
            observeToggleBackOffEvent().removeObserver(toggleBackOffEventObserver)
            observeActivateEvent().removeObserver(activateEventObserver)
            observeDeactivateEvent().removeObserver(deactivateEventObserver)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        info("onDestroyView()")
        viewModel.clearSubscription()
    }

}
