package com.ydly.rankingalarm2.data.remote.model.response

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class NumPeopleResponse(
    @SerializedName("year") @Expose val year: Int,
    @SerializedName("month") @Expose val month: Int,
    @SerializedName("dayOfMonth") @Expose val dayOfMonth: Int,
    @SerializedName("dayNumPeople") @Expose val dayNumPeople: Int,
    @SerializedName("morningNumPeople") @Expose val morningNumPeople: Int
)
