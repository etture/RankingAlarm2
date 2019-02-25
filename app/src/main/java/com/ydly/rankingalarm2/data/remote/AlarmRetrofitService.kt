package com.ydly.rankingalarm2.data.remote

import io.reactivex.Flowable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AlarmRetrofitService {

    @POST("uploadHistory")
    fun uploadAlarmHistory(@Body alarmHistoryBody: AlarmHistoryBody): Flowable<Response<SampleResponse>>

    @POST("testHeader")
    fun testHeader(): Flowable<SampleResponse>

}