package com.ydly.rankingalarm2.ui.alarm

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseFragment
import com.ydly.rankingalarm2.util.GlideApp
import org.jetbrains.anko.info

class SingleAlarmFragment : BaseFragment() {

    companion object {
        fun newInstance() = SingleAlarmFragment()
    }

    private lateinit var viewModel: SingleAlarmViewModel
    private lateinit var binding: com.ydly.rankingalarm2.databinding.FragmentSingleAlarmBinding

    override fun bind(inflater: LayoutInflater, container: ViewGroup?) {
        // ViewModel and DataBinding setup
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_single_alarm, container, false)
        viewModel = ViewModelProviders.of(this).get(SingleAlarmViewModel::class.java)
        binding.viewModel = viewModel
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bind(inflater, container)

        info("onCreateView() called")

        // Observe function in ViewModel to get a new Toast message and display it
        viewModel.observeNewToast().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(activity, toastText, Toast.LENGTH_SHORT).show()
            }
        })

        // Observe function in ViewModel to set initial state of SingleAlarmFragment
        // If alarm from DB is activated, then set ToggleButton to checked
        // If alarm from DB is deactivated, then set ToggleButton to unchecked
        viewModel.observeInitialToggleSetEvent().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { (initialToggledOn, initialHour, initialMinute) ->
                info("observeInitialToggleSetEvent() -> initialToggledOn: $initialToggledOn")
                binding.toggleButton.isChecked = initialToggledOn
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.singleAlarmFragTimePicker.hour = initialHour
                    binding.singleAlarmFragTimePicker.minute = initialMinute
                } else {
                    binding.singleAlarmFragTimePicker.currentHour = initialHour
                    binding.singleAlarmFragTimePicker.currentMinute = initialMinute
                }
            }
        })

        // Observe function in ViewModel to set the ToggleButton back off
        // For situations where the time of the attempted alarm is out of the accepted range (05:00 ~ 10:59)
        viewModel.observeToggleBackOffEvent().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { toggleBackOff ->
                info("observeToggleBackOffEvent() -> toggleBackOff: $toggleBackOff")
                if(!toggleBackOff) {
                    binding.toggleButton.isChecked = toggleBackOff
                }
            }
        })

        // Observe function in ViewModel to get whether alarm was activated or deactivated
        // If activated, then set visibility of TimePicker to GONE and TimeTextView to VISIBLE
        // If deactivated, then set visibility of TimePicker to VISIBLE and TimeTextView to GONE
        viewModel.observeActivateAlarmEvent().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { alarmActivated ->
                if(alarmActivated) {
                    binding.singleAlarmFragTxtVwTime.visibility = View.VISIBLE
                    binding.singleAlarmFragTimePicker.visibility = View.GONE
                } else {
                    binding.singleAlarmFragTxtVwTime.visibility = View.GONE
                    binding.singleAlarmFragTimePicker.visibility = View.VISIBLE
                }
            }
        })



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SingleAlarmViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
