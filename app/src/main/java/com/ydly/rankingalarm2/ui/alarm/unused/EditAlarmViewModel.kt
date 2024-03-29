package com.ydly.rankingalarm2.ui.alarm.unused

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import android.view.View
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
import com.ydly.rankingalarm2.util.SingleEvent
import org.jetbrains.anko.info
import java.util.*

class EditAlarmViewModel : BaseViewModel() {

    // Main Calendar instance storing the set alarm data
    private val calendar = Calendar.getInstance()

    // Today Calendar instance for setting minimum date in DatePicker
    private val today = Calendar.getInstance()

    // For displaying date as String
    private val dateString = MutableLiveData<String>()

    // LiveData to be observed for onClick events
    private val onClickEvent: MutableLiveData<SingleEvent<Int>> = MutableLiveData()

    // Toast event to be observed for showing toasts on View
    private val newToast: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    // For managing AlarmData item in database (edit, delete)
    private lateinit var originalAlarmData: AlarmData

    // For setting TimePicker time (2-way DataBinding)
    val hour = MutableLiveData<Int>()
    val minute = MutableLiveData<Int>()

    // MediatorLiveData for 2-way DataBinding on hour
    private val hourMediator = MediatorLiveData<Int>().apply {
        addSource(hour) { value ->
            setValue(value)
            calendar.set(Calendar.HOUR_OF_DAY, value!!)

            // Check if the time has been past
            // If so, set the date one day forward
            if(timeIsPast()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                buildDateTimeWithCalendar(calendar)
            }

            info("hour changed: ${hour.value}")
        }
    }.also { it.observeForever { /*Do nothing*/ } }

    // MediatorLiveData for 2-way DataBinding on minute
    private val minuteMediator = MediatorLiveData<Int>().apply {
        addSource(minute) { value ->
            setValue(value)
            calendar.set(Calendar.MINUTE, value!!)

            // Check if the time has been past
            // If so, set the date one day forward
            if(timeIsPast()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                buildDateTimeWithCalendar(calendar)
            }

            info("minute changed: ${minute.value}")
        }
    }.also { it.observeForever { /*Do nothing*/ } }


    //========= Init and private functions (business logic) ==========

    init {
        calendar.set(Calendar.MILLISECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
    }

    private fun timeIsPast(): Boolean {
        val rightNow = Calendar.getInstance()
        rightNow.set(Calendar.MILLISECOND, 0)

        // If time has passed
        return if (calendar.timeInMillis <= rightNow.timeInMillis) {
            newToast.value = SingleEvent(res.getString(R.string.timeHasPassed))
            info("cal time: ${calendar.timeInMillis}, now time: ${rightNow.timeInMillis}, comparison: ${rightNow.timeInMillis >= calendar.timeInMillis}")
            true
        } else {
            false
        }
    }

    private fun buildDateTimeWithCalendar(calendar: Calendar) {
        // Initialize DateTimeUtil with milliseconds value from AlarmData object
        val dateTimeUtil = DateTimeUtilMillisToUnits(calendar.timeInMillis)
        buildDateTime(dateTimeUtil)
    }

    private fun buildDateTime(dateTimeUtil: DateTimeUtilMillisToUnits) {
        // Bind Date String
        buildDate(dateTimeUtil)

        // Bind Time values
        buildTime(dateTimeUtil)
    }

    // Function for building the date string displayed on Activity
    private fun buildDate(dateTimeUtil: DateTimeUtilMillisToUnits) {
        var month: String
        val dayOfMonth: String = res.getString(R.string.dayOfMonth, dateTimeUtil.dayOfMonth)
        var dayOfWeek: String

        with(dateTimeUtil.month) {
            month = when (this) {
                Calendar.JANUARY -> res.getString(R.string.jan)
                Calendar.FEBRUARY -> res.getString(R.string.feb)
                Calendar.MARCH -> res.getString(R.string.mar)
                Calendar.APRIL -> res.getString(R.string.apr)
                Calendar.MAY -> res.getString(R.string.may)
                Calendar.JUNE -> res.getString(R.string.jun)
                Calendar.JULY -> res.getString(R.string.jul)
                Calendar.AUGUST -> res.getString(R.string.aug)
                Calendar.SEPTEMBER -> res.getString(R.string.sep)
                Calendar.OCTOBER -> res.getString(R.string.oct)
                Calendar.NOVEMBER -> res.getString(R.string.nov)
                Calendar.DECEMBER -> res.getString(R.string.dec)
                else -> res.getString(R.string.na)
            }
            info("Month/Date: $month $dayOfMonth")
        }

        with(dateTimeUtil.dayOfWeek) {
            dayOfWeek = when (this) {
                Calendar.MONDAY -> res.getString(R.string.mon)
                Calendar.TUESDAY -> res.getString(R.string.tue)
                Calendar.WEDNESDAY -> res.getString(R.string.wed)
                Calendar.THURSDAY -> res.getString(R.string.thu)
                Calendar.FRIDAY -> res.getString(R.string.fri)
                Calendar.SATURDAY -> res.getString(R.string.sat)
                Calendar.SUNDAY -> res.getString(R.string.sun)
                else -> res.getString(R.string.na)
            }
            info("Day of Week: $dayOfWeek")
        }

        dateString.value = "$month $dayOfMonth ($dayOfWeek)"
        info("Full dayOfMonth string: ${dateString.value}")
    }

    // Function for updating time for displaying on TimePicker
    private fun buildTime(dateTimeUtil: DateTimeUtilMillisToUnits) {
        hour.value = dateTimeUtil.hour24
        minute.value = dateTimeUtil.minute
    }

    // For use when initializing with AlarmData timeInMillis property
    private fun setCalendarWithMillis(timeInMillis: Long) {
        calendar.timeInMillis = timeInMillis
        buildDateTimeWithCalendar(calendar)
    }


    //========= Functions accessible by View (data manipulation) ==========

    fun getOriginalAlarmData(): AlarmData = originalAlarmData

    fun getCalendar(): Calendar = calendar
    fun getToday(): Calendar = today

    fun getYear(): Int = calendar.get(Calendar.YEAR)
    fun getMonth(): Int = calendar.get(Calendar.MONTH)
    fun getDayOfMonth(): Int = calendar.get(Calendar.DAY_OF_MONTH)
    fun getHour(): Int? = calendar.get(Calendar.HOUR_OF_DAY)
    fun getMinute(): Int? = calendar.get(Calendar.MINUTE)

    // Return whether date/time has been changed by user
    fun hasBeenChanged(): Boolean = calendar.timeInMillis != originalAlarmData.timeInMillis

    // View observes this to get onClick events on Views
    fun observeOnClickEvent(): LiveData<SingleEvent<Int>> = onClickEvent

    // View observes this to show new Toast messages
    fun observeNewToast(): LiveData<SingleEvent<String>> = newToast

    fun initAlarmData(alarmData: AlarmData) {
        originalAlarmData = alarmData
        setCalendarWithMillis(alarmData.timeInMillis)
    }

    //  For setting date with DatePickerDialog
    fun setCalendarWithDate(year: Int, month: Int, dayOfMonth: Int) {
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        buildDateTimeWithCalendar(calendar)
    }


    //========= Functions accessible by View (DataBinding) ==========

    fun getDateString(): LiveData<String> = dateString

    fun onClick(view: View) {
        onClickEvent.value = SingleEvent(view.id)
    }

}
