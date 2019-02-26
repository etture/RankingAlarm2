package com.ydly.rankingalarm2.ui.alarm.unused

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.ydly.rankingalarm2.R
import com.ydly.rankingalarm2.base.BaseViewModel
import com.ydly.rankingalarm2.data.local.alarm.model.AlarmData
import com.ydly.rankingalarm2.util.DateTimeUtilMillisToUnits
import org.jetbrains.anko.info
import java.util.*

class AlarmItemViewModel : BaseViewModel() {

    private var alarmData: AlarmData =
        AlarmData(id = 0, timeInMillis = 0, isToggledOn = false)
    private lateinit var alarmItemListener: AlarmListAdapter.AlarmItemListener

    // 1-way DataBinding
    private val ampm = MutableLiveData<String>()
    private val time = MutableLiveData<String>()
    private val date = MutableLiveData<String>()

    // 2-way DataBinding
    var isToggledOn = MutableLiveData<Boolean>().apply { value = false }

    private val isToggledOnMediator = MediatorLiveData<Boolean>().apply {
        addSource(isToggledOn) { value ->
            setValue(value)
            alarmData.isToggledOn = value!!
            info("isToggledOn 2-way value changed, alarmData: $alarmData, boolean: $value")
        }
    }.also { it.observeForever { /*Do nothing*/ } }


    //========= Init and private functions (business logic) ==========

    init {
//        alarmData = AlarmData(id = 0, timeInMillis = 0, isToggledOn = false)
    }

    private fun buildTime(dateTimeUtil: DateTimeUtilMillisToUnits) {
        var hour: String
        var minute: String

        with(dateTimeUtil.hour) {
            hour = when (this < 10) {
                true -> "0$this"
                false -> this.toString()
            }
            info("Hour: $hour")
        }

        with(dateTimeUtil.minute) {
            minute = when (this < 10) {
                true -> "0$this"
                false -> this.toString()
            }
            info("Minute: $minute")
        }

        time.value = "$hour:$minute"
    }

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
            dayOfWeek = when(this) {
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

        date.value = "$month $dayOfMonth ($dayOfWeek)"
        info("Full dayOfMonth string: ${date.value}")
    }


    //========= Functions accessible by View (data manipulation) ==========

    fun bind(alarmData: AlarmData, listener: AlarmListAdapter.AlarmItemListener) {

        info("initialize() called, ${alarmData.timeInMillis}")

        // Initialize DateTimeUtil with milliseconds value from AlarmData object
        val dateTimeUtil = DateTimeUtilMillisToUnits(alarmData.timeInMillis)

        // Bind AM/PM String
        when (dateTimeUtil.ampm) {
            Calendar.AM -> ampm.value = res.getString(R.string.am)
            Calendar.PM -> ampm.value = res.getString(R.string.pm)
        }

        // Bind Time String
        buildTime(dateTimeUtil)

        // Bind Date String
        buildDate(dateTimeUtil)

        // Bind isToggledOn state
        this.isToggledOn.value = alarmData.isToggledOn
        info("initialize() -> id: ${alarmData.id} initial isToggledOn: ${alarmData.isToggledOn}")

        this.alarmData = alarmData
        alarmItemListener = listener

    }

    fun onClick() {
        alarmItemListener.onAlarmItemClick(alarmData)
    }

    fun onToggleChanged(isToggledOn: Boolean) {
        // reflect toggle state to ViewModel (data-binded value)
//        this.isToggledOn.value = isToggledOn

        // reflect toggle state to DB
        alarmItemListener.onOnOffToggleChanged(alarmData, isToggledOn)
        info("toggle changed: $isToggledOn")
    }


    //========= Functions accessible by View (DataBinding) ==========

    fun getAmPm(): LiveData<String> = ampm
    fun getTime(): LiveData<String> = time
    fun getDate(): LiveData<String> = date
//    fun getToggledState(): LiveData<Boolean> = isToggledOn
}
