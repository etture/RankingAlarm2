package com.ydly.rankingalarm2.data.remote

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SampleResponse (
    @SerializedName("message") @Expose val message: String
)