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
    var timeInMillis: Long

    init {

        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        this.timeInMillis = calendar.timeInMillis

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

    fun add(field: Int, amount: Int) {
        val calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        calendar.add(field, amount)
        reset(calendar.timeInMillis)
    }

    private fun reset(timeInMillis: Long) {
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

    companion object {
        fun millisToString(millis: Long): String {
            val cal: Calendar = Calendar.getInstance()
            cal.timeInMillis = millis
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH)
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val h = cal.get(Calendar.HOUR_OF_DAY)
            val min = cal.get(Calendar.MINUTE)
            val s = cal.get(Calendar.SECOND)
            val mil = cal.get(Calendar.MILLISECOND)

            return "($y/${m+1}/$d $h:$min:$s:$mil)"
        }
    }

}
