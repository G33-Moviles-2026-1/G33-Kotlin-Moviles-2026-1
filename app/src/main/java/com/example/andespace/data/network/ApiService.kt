package com.example.andespace.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val first_semester: String
)

typealias MeResponse = Any

interface ApiService {
    @POST("signup/")
    suspend fun register(@Body request: RegisterRequest): Response<Any>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<Any>

    @GET("me/")
    suspend fun checkSession(): Response<MeResponse>

    @POST("logout/")
    suspend fun logout(): Response<Unit>
}