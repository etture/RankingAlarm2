package com.ydly.rankingalarm2.data.local.alarm

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "alarmHistoryData",
    indices = [Index(value = ["year", "month", "dayOfMonth"], unique = true)]
)
data class AlarmHistoryData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,

    // year + month + dayOfMonth --> UNIQUE constraint
    @ColumnInfo(name = "year")
    var year: Int,

    @ColumnInfo(name = "month")
    var month: Int,

    @ColumnInfo(name = "dayOfMonth")
    var dayOfMonth: Int,

    @ColumnInfo(name = "hour")
    var hour: Int,

    @ColumnInfo(name = "minute")
    var minute: Int,

    // String ID of time zone at the time the alarm rings
    // e.g. "America/Los_Angeles", "Asia/Seoul"
    @ColumnInfo(name = "timeZoneId")
    var timeZoneId: String,

    // Millis time of 00:00 at the time of the alarm ringing
    // Used to determine time zone and relative time
    @ColumnInfo(name = "baseTimeInMillis")
    var baseTimeInMillis: Long,

    // Time when this alarm was set to ring at
    @ColumnInfo(name = "alarmTimeInMillis")
    var alarmTimeInMillis: Long,

    // The time taken from the time the alarm rang
    // until user turned the alarm off
    // Calculate alarmTimeInMillis + takenTimeInMillis for wokenTime
    // If this is null and rangToday == false, user snoozed or turned off the phone
    @ColumnInfo(name = "takenTimeInMillis")
    var takenTimeInMillis: Long? = null,

    // Boolean value showing whether the alarm was successfully turned off
    // Alarm rings once per day, so possible to express in T/F
    @ColumnInfo(name = "wokeUp")
    var wokeUp: Boolean = false

) : Parcelable
