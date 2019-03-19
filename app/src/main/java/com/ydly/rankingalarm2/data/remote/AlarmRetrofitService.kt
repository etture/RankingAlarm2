package com.ydly.rankingalarm2.data.remote

import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.model.response.NumPeopleResponse
import com.ydly.rankingalarm2.data.remote.model.response.PendingListResponse
import com.ydly.rankingalarm2.data.remote.model.response.SampleResponse
import com.ydly.rankingalarm2.data.remote.model.response.UploadResponse
import io.reactivex.Flowable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.POST

interface AlarmRetrofitService {

    @POST("rank/uploadAndGetRank")
    fun uploadAlarmHistory(@Body alarmHistoryBody: AlarmHistoryBody): Flowable<Response<UploadResponse>>

    @POST("rank/uploadPendingList")
    fun uploadPendingHistoryList(@Body pendingHistoryList: List<AlarmHistoryBody>): Flowable<Response<PendingListResponse>>

    @POST("rank/fetchNumPeople")
    fun fetchNumPeople(@Field("year") year: Int, @Field("month") month: Int, @Field("dayOfMonth") dayOfMonth: Int): Flowable<Response<NumPeopleResponse>>

    @POST("test/testHeader")
    fun testHeader(): Flowable<SampleResponse>

}
