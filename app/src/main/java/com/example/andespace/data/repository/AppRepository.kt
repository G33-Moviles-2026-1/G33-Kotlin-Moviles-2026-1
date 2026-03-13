package com.example.andespace.data.repository

import kotlinx.coroutines.delay

class AppRepository {

    suspend fun getUserName(): String {
        delay(1000)
        return "Estudiante Uniandes"
    }
}
