package com.ydly.rankingalarm2.data.remote.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UploadResponse (
    @SerializedName("message") @Expose val message: String,
    @SerializedName("dayRank") @Expose val dayRank: Int,
    @SerializedName("morningRank") @Expose val morningRank: Int
)
