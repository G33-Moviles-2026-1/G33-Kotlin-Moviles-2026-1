package com.example.andespace.data.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(
    private val cookieJar: SessionCookieJar
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        if (!NetworkMonitor.isOnline.value) {
            NetworkMonitor.forceRetryConnection()
            val isRecovered = NetworkMonitor.verifyConnectionNow()
            if (!isRecovered) {
                throw NoInternetException("Aborted: Device is currently offline.")
            }
        }

        val request = chain.request()

        try {
            val response = chain.proceed(request)
            NetworkMonitor.reportNetworkSuccess()

            if (response.code == 401) {
                cookieJar.clearCookies()
                throw SessionExpiredException()
            }
            return response

        } catch (e: IOException) {
            if (e !is SessionExpiredException && e !is NoInternetException) {
                NetworkMonitor.reportNetworkError()
            }
            throw e
        }
    }
}

class SessionExpiredException : IOException("Session expired. Automatically logging out.")
class NoInternetException(message: String) : IOException(message)