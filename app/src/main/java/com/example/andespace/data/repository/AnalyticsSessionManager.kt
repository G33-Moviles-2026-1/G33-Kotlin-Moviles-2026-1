package com.example.andespace.data.repository

import java.util.UUID

object AnalyticsSessionManager {
    val currentSessionId: String = UUID.randomUUID().toString()
}