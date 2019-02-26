package com.ydly.rankingalarm2.data.local.alarm.model

import androidx.room.*
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(
    tableName = "alarmData",
    indices = [Index(value = ["timeInMillis"], unique = true)]
)
data class AlarmData(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,

    @ColumnInfo(name = "timeInMillis")
    var timeInMillis: Long,

    @ColumnInfo(name = "isToggledOn")
    var isToggledOn: Boolean = false

) : Parcelable
