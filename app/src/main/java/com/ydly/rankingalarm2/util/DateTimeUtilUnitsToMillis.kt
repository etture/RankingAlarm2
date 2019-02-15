package com.ydly.rankingalarm2.util

import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.util.*

class DateTimeUtilUnitsToMillis(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) : AnkoLogger {

    private val calendar: Calendar = Calendar.getInstance()

    init {

        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

        info("Month: ${calendar.get(Calendar.MONTH)}, Date: ${calendar.get(Calendar.DAY_OF_MONTH)}, Hour: $hour")

        // Set the alarm time set with TimePicker and set milliseconds to 0
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        info("before setting millis: ${calendar.timeInMillis}")

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        info("as of setting millis: ${calendar.timeInMillis}")

    }

    fun getDateTimeInMillis() = calendar.timeInMillis

}
