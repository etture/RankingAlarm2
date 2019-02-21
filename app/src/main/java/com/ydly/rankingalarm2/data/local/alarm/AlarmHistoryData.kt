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

    // If this is null, it means user did not set an alarm this day
    @ColumnInfo(name = "alarmTimeInMillis")
    var alarmTimeInMillis: Long? = null,

    // The time taken from the time the alarm rang
    // until user turned the alarm off
    // Calculate alarmTimeInMillis + takenTimeInMillis for wokenTime
    // If this is null and rangToday == false, no alarm rang on this day
    // If this is null and rangToday == true, then user didn't properly turn off the alarm
    @ColumnInfo(name = "takenTimeInMillis")
    var takenTimeInMillis: Long? = null,

    // Boolean value showing whether the alarm was successfully turned off
    // Alarm rings once per day, so possible to express in T/F
    @ColumnInfo(name = "wokeUp")
    var wokeUp: Boolean = false

) : Parcelable
