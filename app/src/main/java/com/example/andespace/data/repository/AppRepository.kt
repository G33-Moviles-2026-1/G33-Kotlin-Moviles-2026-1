package com.example.andespace.data.repository

import kotlinx.coroutines.delay

class AppRepository {

    suspend fun getUserName(): String {
        delay(1000)
        return "Estudiante Uniandes"
    }

    suspend fun login(user: String, password: String): Boolean {
        delay(1000) // Simular latencia
        return user == "Kotlin" && password == "123"
    }

    // Ejemplo de obtención de datos para una funcionalidad futura
    fun getHistory() = listOf("ML 001", "W 101", "SD 202")
}
