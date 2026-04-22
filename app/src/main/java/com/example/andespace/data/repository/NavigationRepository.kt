package com.example.andespace.data.repository

import com.example.andespace.data.network.ApiService
import com.example.andespace.data.repository.shared.extractErrorMessage
import com.example.andespace.model.dto.NavigationPathResponse
import com.example.andespace.model.dto.NavigationPathSearchParams
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class NavigationRepository(
    private val apiService: ApiService
) {

    suspend fun getNavigationPath(
        params: NavigationPathSearchParams
    ): Result<NavigationPathResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNavigationPath(
                fromRoom = params.fromClassroom,
                toRoom = params.toClassroom
            )
            if (response.isSuccessful) {
                val path = response.body() ?: throw Exception("Empty response body")
                Result.success(path)
            } else {
                val rawErrorBody = response.errorBody()?.string()
                Result.failure(Exception(extractErrorMessage(rawErrorBody, response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("No internet connection. Please check your network and try again."))
        }
    }
}
