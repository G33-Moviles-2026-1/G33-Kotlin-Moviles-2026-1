package com.example.andespace.data.network

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class LocalhostFallbackInterceptor(
    private val primaryBaseUrl: String,
    private val fallbackBaseUrl: String
) : Interceptor {

    private val fallbackHttpUrl: HttpUrl = fallbackBaseUrl.toHttpUrlOrNull()
        ?: throw IllegalArgumentException("Invalid fallback base URL: $fallbackBaseUrl")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val isPrimaryHostRequest = request.url.host == primaryHost()

        return try {
            val primaryResponse = chain.proceed(request)
            if (isPrimaryHostRequest && primaryResponse.code >= 500) {
                primaryResponse.close()
                chain.proceed(request.withFallbackHost(fallbackHttpUrl))
            } else {
                primaryResponse
            }
        } catch (primaryError: IOException) {
            if (!isPrimaryHostRequest) throw primaryError
            chain.proceed(request.withFallbackHost(fallbackHttpUrl))
        }
    }

    private fun primaryHost(): String {
        val parsedPrimary = primaryBaseUrl.toHttpUrlOrNull()
            ?: throw IllegalArgumentException("Invalid primary base URL: $primaryBaseUrl")
        return parsedPrimary.host
    }

    private fun okhttp3.Request.withFallbackHost(fallbackUrl: HttpUrl): okhttp3.Request {
        val rewrittenUrl = url.newBuilder()
            .scheme(fallbackUrl.scheme)
            .host(fallbackUrl.host)
            .port(fallbackUrl.port)
            .build()
        return newBuilder().url(rewrittenUrl).build()
    }
}
