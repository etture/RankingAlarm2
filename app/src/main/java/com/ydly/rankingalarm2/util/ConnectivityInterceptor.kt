package com.ydly.rankingalarm2.util

import android.net.ConnectivityManager
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

// Interceptor to add to OkHttp client for checking internet connection before doing anything with Retrofit
class ConnectivityInterceptor(private val cm: ConnectivityManager): Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        if(!isConnected()) {
            throw OfflineException("No internet connection")
        }

        return chain.proceed(chain.request())
    }

    private fun isConnected(): Boolean {
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    class OfflineException(msg: String): IOException(msg)

}