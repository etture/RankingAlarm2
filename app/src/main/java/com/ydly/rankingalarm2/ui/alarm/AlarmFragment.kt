package com.ydly.rankingalarm2.ui.alarm

import android.annotation.TargetApi
import android.app.AlarmManager
import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment
import com.ydly.rankingalarm2.receiver.AlarmReceiver
import com.ydly.rankingalarm2.util.CREATE_ALARM_ACTIVITY
import com.ydly.rankingalarm2.util.EDIT_ALARM_ACTIVITY
import org.jetbrains.anko.info

class AlarmFragment : BaseFragment() {

    private lateinit var viewModel: AlarmViewModel
    private lateinit var binding: com.ydly.rankingalarm2.databinding.FragmentAlarmBinding

    private lateinit var dateUpdatedToNextReceiver: BroadcastReceiver

    private val alarmManagerWrapper = AlarmManagerWrapper(alarmManager)

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) {
        // ViewModel and DataBinding setup
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm, container, false)
        viewModel = ViewModelProviders.of(this).get(AlarmViewModel::class.java)
        binding.viewModel = viewModel

        // RecyclerView setup (adapter setup done via DataBinding in XML + BindingAdapter)
        binding.alarmFragRcyViewAlarmList.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.alarmFragRcyViewAlarmList.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        bind(inflater, container)
        info("onCreateView() called")

        // Observe function in ViewModel to get onClick events from Views
        // Take when(viewId) to perform different task for each View
        viewModel.observeOnClickEvent().observe(this, Observer {
            it?.takeIf { true }?.getContentIfNotHandled()?.let { viewId ->
                when (viewId) {
                    binding.alarmFragFabAddAlarmBtn.id -> {
                        val fabIntent = Intent(activity, CreateAlarmActivity::class.java)
                        startActivityForResult(fabIntent, CREATE_ALARM_ACTIVITY)
                    }
                    else -> {/*Do nothing*/
                    }
                }
            }
        })

        // Observe function in ViewModel to get onClick event on RecyclerView items
        // Start EditAlarmActivity with id and timeInMillis from alarmData
        viewModel.observeOnListItemClickEvent().observe(this, Observer {
            it?.takeIf { true }?.getContentIfNotHandled()?.let { alarmData ->
                val listItemIntent = Intent(activity, EditAlarmActivity::class.java).apply {
                    putExtra("alarmData", alarmData)
                }
                startActivityForResult(listItemIntent, EDIT_ALARM_ACTIVITY)
            }
        })

        // Observe function in ViewModel to get a new Toast message and display it
        viewModel.observeNewToast().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(activity, toastText, Toast.LENGTH_SHORT).show()
            }
        })

        // Observe function in ViewModel to get activateAlarmEvent
        viewModel.observeActivateAlarmEvent().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { alarmData ->
                info("activateAlarmEvent.observe(): $alarmData")

                val idInteger = alarmData.id?.toInt()
                val alarmTime = alarmData.timeInMillis

                val activateAlarmIntent = Intent(activity, AlarmReceiver::class.java)
                activateAlarmIntent.putExtra("message", alarmData.toString())

                // PendingIntent to send broadcast to AlarmReceiver at specified time
                // Use alarmData.id as 2nd parameter (requestCode),
                // which uniquely identifies this PendingIntent
                val pendingIntent = PendingIntent.getBroadcast(
                    activity,
                    idInteger!!,
                    activateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Set AlarmManager with PendingIntent at the specified time
                // For exact time-setting, vary functions based on API version
                with(Build.VERSION.SDK_INT) {
                    when {
                        this >= Build.VERSION_CODES.M -> {
                            alarmManagerWrapper.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent
                            )
                        }
                        this >= Build.VERSION_CODES.LOLLIPOP -> {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                        }
                        else -> {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent)
                        }
                    }
                }

            }
        })

        // Observe function in ViewModel to get deactivateAlarmEvent
        viewModel.observeDeactivateAlarmEvent().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { alarmData ->
                info("deactivateAlarmEvent.observe(): $alarmData")

                val idInteger = alarmData.id?.toInt()

                val deactivateAlarmIntent = Intent(activity, AlarmReceiver::class.java)
                deactivateAlarmIntent.putExtra("message", alarmData.toString())

                // PendingIntent to send broadcast to AlarmReceiver at specified time
                // Use alarmData.id as 2nd parameter (requestCode),
                // which uniquely identifies this PendingIntent
                val pendingIntent = PendingIntent.getBroadcast(
                    activity,
                    idInteger!!,
                    deactivateAlarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )

                // Both PendingIntent and AlarmManager must be cancelled to completely deactivate the alarm
                pendingIntent.cancel()
                alarmManager.cancel(pendingIntent)
            }
        })

        return binding.root
    }

    // AlarmManger wrapper class to use @TargetApi annotation
    internal class AlarmManagerWrapper(private val alarmManager: AlarmManager) {
        @TargetApi(23)
        fun setExactAndAllowWhileIdle(type: Int, triggerAtMillis: Long, operation: PendingIntent) {
            alarmManager.setExactAndAllowWhileIdle(type, triggerAtMillis, operation)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        info("onCreate() called")
    }

    override fun onResume() {
        super.onResume()
        info("onResume() called")

        // Since the alarm list may have changed through TimeUpdateService while
        // the Activity was off-screen, refresh the list at onResume()
        viewModel.refreshAlarmList()

        // Receive ACTION_TIME_TICK every minute
        // and start TimeUpdateService with the current timeInMillis
        // Registering receiver programmatically because Intent.ACTION_TIME_TICK cannot be registered via Manifest
        val timeTickIntentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        activity?.registerReceiver(minuteTickReceiver, timeTickIntentFilter)

        // Receive broadcast from TimeUpdateService that dates were changed on some items
        // and refresh the alarm list
        dateUpdatedToNextReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val datesWereUpdated: Boolean = intent?.getBooleanExtra("datesUpdated", false)!!
                if (datesWereUpdated) viewModel.refreshAlarmList()
            }
        }
        LocalBroadcastManager.getInstance(activity!!)
            .registerReceiver(dateUpdatedToNextReceiver, IntentFilter("datesUpdated"))

    }

    override fun onPause() {
        super.onPause()
        info("onPause() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        info("onDestroy() called")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        info("onDestroyView() called")

        activity?.unregisterReceiver(minuteTickReceiver)
        LocalBroadcastManager.getInstance(activity!!).unregisterReceiver(dateUpdatedToNextReceiver)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AlarmFragment()
    }

}
