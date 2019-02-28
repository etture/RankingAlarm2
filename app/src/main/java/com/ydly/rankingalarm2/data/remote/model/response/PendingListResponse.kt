package com.ydly.rankingalarm2.data.remote.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PendingListResponse(
    @SerializedName("message") @Expose val message: String,
    @SerializedName("rankInfos") @Expose val rankInfos: List<UploadResponse>,
    @SerializedName("successIdInDeviceList") @Expose val successIdInDeviceList: List<Long>
)
