package com.ydly.rankingalarm2.data.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AlarmHistoryBody (
    @SerializedName("userUUID") @Expose val userUUID: String,
    @SerializedName("year") @Expose val year: Int,
    @SerializedName("month") @Expose val month: Int,
    @SerializedName("dayOfMonth") @Expose val dayOfMonth: Int,
    @SerializedName("hour") @Expose val hour: Int,
    @SerializedName("minute") @Expose val minute: Int,
    @SerializedName("timeZoneId") @Expose val timeZoneId: String,
    @SerializedName("baseTimeInMillis") @Expose val baseTimeInMillis: Long,
    @SerializedName("alarmTimeInMillis") @Expose val alarmTimeInMillis: Long,
    @SerializedName("takenTimeInMillis") @Expose val takenTimeInMillis: Long?,
    @SerializedName("wokeUp") @Expose val wokeUp: Boolean
)