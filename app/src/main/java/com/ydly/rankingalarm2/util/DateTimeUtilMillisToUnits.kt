package com.ydly.rankingalarm2.util

import java.util.*

class DateTimeUtilMillisToUnits(timeInMillis: Long) {

    // Date variables
    var year: Int
    var month: Int
    var dayOfMonth: Int
    var dayOfWeek: Int

    // Time variables
    var hour: Int
    var minute: Int
    var ampm: Int

    var hour24: Int

    init {

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis

        // Initialize dayOfMonth variables
        year = calendar.get(Calendar.YEAR)
        month = calendar.get(Calendar.MONTH)
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Initialize time variables
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        ampm = calendar.get(Calendar.AM_PM)

        hour24 = calendar.get(Calendar.HOUR_OF_DAY)

    }

}
