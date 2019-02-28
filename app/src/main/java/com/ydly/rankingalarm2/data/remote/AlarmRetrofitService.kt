package com.ydly.rankingalarm2.data.remote

import com.ydly.rankingalarm2.data.remote.model.request.AlarmHistoryBody
import com.ydly.rankingalarm2.data.remote.model.response.PendingListResponse
import com.ydly.rankingalarm2.data.remote.model.response.SampleResponse
import com.ydly.rankingalarm2.data.remote.model.response.UploadResponse
import io.reactivex.Flowable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AlarmRetrofitService {

    @POST("rank/uploadAndGetRank")
    fun uploadAlarmHistory(@Body alarmHistoryBody: AlarmHistoryBody): Flowable<Response<UploadResponse>>

    @POST("rank/uploadPendingList")
    fun uploadPendingHistoryList(@Body pendingHistoryList: List<AlarmHistoryBody>): Flowable<Response<PendingListResponse>>

    @POST("test/testHeader")
    fun testHeader(): Flowable<SampleResponse>

}
