package com.example.andespace.data.location

data class GeoLocation(
    val latitude: Double,
    val longitude: Double
)

interface LocationSensor {
    suspend fun getCurrentLocation(): GeoLocation?
}
