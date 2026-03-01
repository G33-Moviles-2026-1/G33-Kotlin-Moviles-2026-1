package com.example.andespace.data.repository

import kotlinx.coroutines.delay

/**
 * Capa de DATOS (Repository).
 * Aquí es donde se decide si los datos vienen de una API, base de datos local o mock.
 * El ViewModel NO sabe de dónde vienen los datos, solo llama al repositorio.
 */
class AppRepository {
    
    // Simula una carga de datos desde internet o DB
    suspend fun getUserName(): String {
        delay(1000) // Simular latencia
        return "Estudiante Uniandes"
    }

    // Ejemplo de obtención de datos para una funcionalidad futura
    fun getHistory() = listOf("ML 001", "W 101", "SD 202")
}
