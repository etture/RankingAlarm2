package com.ydly.rankingalarm2.injection.module

import android.net.ConnectivityManager
import com.ydly.rankingalarm2.data.remote.AlarmRetrofitService
import com.ydly.rankingalarm2.injection.scope.RepositoryScope
import com.ydly.rankingalarm2.util.BASE_URL
import com.ydly.rankingalarm2.util.ConnectivityInterceptor
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Suppress("unused")
@Module(includes = [AppModule::class])
class RetrofitModule {

    @RepositoryScope
    @Provides
    fun provideRetrofitBuilder(): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
    }

    @RepositoryScope
    @Provides
    fun provideConnectivityInterceptor(cm: ConnectivityManager): ConnectivityInterceptor = ConnectivityInterceptor(cm)

    @RepositoryScope
    @Provides
    fun provideAuthTokenRetrofitInterface(retrofitBuilder: Retrofit.Builder, ci: ConnectivityInterceptor): Retrofit {
        val authTokenClient = OkHttpClient.Builder()
            .addInterceptor(ci)
            .addInterceptor { chain ->
                val request = chain.request()
                val newRequest = request.newBuilder()
                    .addHeader("authorization", "rankingAlarmAuthToken-91fa18m")
                    .build()
                chain.proceed(newRequest)
            }
//            .connectTimeout(2, TimeUnit.SECONDS)
//            .writeTimeout(2, TimeUnit.SECONDS)
//            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        retrofitBuilder.client(authTokenClient)
        return retrofitBuilder.build()
    }

    @RepositoryScope
    @Provides
    fun provideAlarmRetrofitService(retrofit: Retrofit): AlarmRetrofitService {
        return retrofit.create(AlarmRetrofitService::class.java)
    }

}
