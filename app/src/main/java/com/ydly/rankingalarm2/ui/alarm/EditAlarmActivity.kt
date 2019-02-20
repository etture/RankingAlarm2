package com.ydly.rankingalarm2.ui.alarm

import android.app.DatePickerDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.databinding.DataBindingUtil
import android.os.Bundle
import android.widget.Toast
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseActivity
import com.ydly.rankingalarm2.util.DELETE_ALARM
import com.ydly.rankingalarm2.util.DateTimeUtilUnitsToMillis
import com.ydly.rankingalarm2.util.EDIT_ALARM
import com.ydly.rankingalarm2.util.NO_CHANGE_ALARM
import java.util.*

class EditAlarmActivity : BaseActivity() {

    private lateinit var binding: com.ydly.rankingalarm2.databinding.ActivityEditAlarmBinding
    private lateinit var viewModel: EditAlarmViewModel

    override fun initialize() {
        // ViewModel and DataBinding setup (called at onCreate())
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_alarm)
        viewModel = ViewModelProviders.of(this).get(EditAlarmViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Initialize ViewModel with particular AlarmData information (id and timeInMillis)
        viewModel.initAlarmData(intent.getParcelableExtra("alarmData"))
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

        viewModel.observeOnClickEvent().observe(this, Observer {
            it?.takeIf { true }?.getContentIfNotHandled()?.let { viewId ->
                when(viewId) {

                    // When the save-alarm button is pressed
                    // Check whether the alarm date/time was changed or not
                    // If no change, just finish the activity with NO_CHANGE_ALARM flag
                    // If there has been a change,
                    // send the dateTimeInMillis back to the previous Activity/Fragment
                    // where it will be updated via ViewModel
                    binding.editAlarmBtnSave.id -> {

                        val saveAlarmIntent = Intent()

                        if(viewModel.hasBeenChanged()) {

                            val dateTimeUtil =
                                DateTimeUtilUnitsToMillis(
                                    viewModel.getYear(),
                                    viewModel.getMonth(),
                                    viewModel.getDayOfMonth(),
                                    viewModel.getHour()!!,
                                    viewModel.getMinute()!!
                                )

                            // Pass EDIT_ALARM flag back to previous Activity/Fragment
                            // Pass the new dateTimeInMillis and id back to previous Activity/Fragment
                            saveAlarmIntent.putExtra("status", EDIT_ALARM)
                            saveAlarmIntent.putExtra("editedAlarmInMillis", dateTimeUtil.getDateTimeInMillis())
                            saveAlarmIntent.putExtra("originalAlarmData", viewModel.getOriginalAlarmData())

                        } else {
                            // Pass NO_CHANGE_ALARM flag back to previous Activity/Fragment
                            saveAlarmIntent.putExtra("status", NO_CHANGE_ALARM)
                        }
                        setResult(RESULT_OK, saveAlarmIntent)
                        finish()
                    }

                    // When the cancel button is pressed
                    // finish the Activity with resultCode RESULT_CANCELED
                    binding.editAlarmBtnCancel.id -> {
                        setResult(RESULT_CANCELED)
                        finish()
                    }

                    // When the delete button is pressed
                    // pass the DELETE_ALARM flag, id, and originalTimeInMillis
                    // and finish the Activity with resultCode RESULT_OK
                    binding.editAlarmBtnDelete.id -> {
                        val deleteAlarmIntent = Intent().apply {
                            putExtra("status", DELETE_ALARM)
                            putExtra("originalAlarmData", viewModel.getOriginalAlarmData())
                        }
                        setResult(RESULT_OK, deleteAlarmIntent)
                        finish()
                    }

                    // When DatePicker button is pressed
                    // Show the pre-created datePickerDialog after updating the date
                    binding.editAlarmBtnDatePicker.id -> {
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
