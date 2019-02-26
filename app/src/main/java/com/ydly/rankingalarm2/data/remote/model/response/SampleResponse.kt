package com.ydly.rankingalarm2.data.remote.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SampleResponse (
    @SerializedName("message") @Expose val message: String
)