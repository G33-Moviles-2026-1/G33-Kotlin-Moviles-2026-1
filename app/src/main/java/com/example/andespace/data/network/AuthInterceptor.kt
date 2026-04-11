package com.example.andespace.data.network

import com.example.andespace.data.repository.AppRepository
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val cookieJar: SessionCookieJar) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            AppRepository.reportNetworkSuccess()

            if (response.code == 401) {
                cookieJar.clearCookies()
                throw SessionExpiredException()
            }

            return response

        } catch (e: IOException) {
            if (e !is SessionExpiredException) {
                AppRepository.reportNetworkError()
            }
            throw e
        }
    }
}
class SessionExpiredException : IOException("Session expired. Automatically logging out.")