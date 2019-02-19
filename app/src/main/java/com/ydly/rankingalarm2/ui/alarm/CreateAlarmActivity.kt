package com.ydly.rankingalarm2.ui.alarm

import android.app.Activity
import android.app.DatePickerDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import com.ydly.rankingalarm2.util.DateTimeUtilUnitsToMillis
import java.util.*

class CreateAlarmActivity : BaseActivity() {

    private lateinit var viewModel: CreateAlarmViewModel
    private lateinit var binding: com.ydly.rankingalarm2.databinding.ActivityCreateAlarmBinding

    override fun bind() {
        // ViewModel and DataBinding setup
        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_alarm)
        viewModel = ViewModelProviders.of(this).get(CreateAlarmViewModel::class.java)
        binding.viewModel = viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DatePickerDialog setup for use when selecting date
        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                viewModel.setCalendarWithDate(
                    year,
                    month,
                    dayOfMonth
                )
            },
            viewModel.getCalendar().get(Calendar.YEAR),
            viewModel.getCalendar().get(Calendar.MONTH),
            viewModel.getCalendar().get(Calendar.DAY_OF_MONTH)
        )

        // Set DatePicker so that dates before today cannot be picked
        datePickerDialog.datePicker.minDate = viewModel.getToday().timeInMillis

        // Observe function in ViewModel to get onClick events from Views
        // Take when(viewId) to perform different task for each View
        viewModel.observeOnClickEvent().observe(this, Observer {
            it?.takeIf { true }?.getContentIfNotHandled()?.let { viewId ->
                when (viewId) {

                    // When the set-alarm button is pressed
                    // send the dateTimeInMillis back to the previous Activity/Fragment, where it will be inserted via ViewModel
                    binding.createAlarmBtnSet.id -> {
                        val dateTimeUtil =
                            DateTimeUtilUnitsToMillis(
                                viewModel.getYear(),
                                viewModel.getMonth(),
                                viewModel.getDayOfMonth(),
                                viewModel.getHour()!!,
                                viewModel.getMinute()!!
                            )

                        // Pass the new dateTimeInMillis back to previous Activity/Fragment
                        val createAlarmIntent = Intent()
                        createAlarmIntent.putExtra("newAlarmInMillis", dateTimeUtil.getDateTimeInMillis())
                        setResult(Activity.RESULT_OK, createAlarmIntent)
                        finish()
                    }

                    // When the cancel button is pressed
                    // finish the Activity with resultCode RESULT_CANCELED
                    binding.createAlarmBtnCancel.id -> {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }

                    // When DatePicker button is pressed
                    // Show the pre-created datePickerDialog after updating the date
                    binding.createAlarmBtnDatePicker.id -> {
                        viewModel.getCalendar().let { cal ->
                            datePickerDialog.updateDate(
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                        }
                        datePickerDialog.show()
                    }

                }
            }
        })

        // Observe function in ViewModel to get a new Toast message and display it
        viewModel.observeNewToast().observe(this, Observer {
            it?.getContentIfNotHandled()?.let { toastText ->
                Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show()
            }
        })
    }

}
