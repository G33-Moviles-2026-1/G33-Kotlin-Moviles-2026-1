package com.example.andespace.data.model

data class HomeSearchParams(
    val classroom: String,
    val date: String,
    val since: String?,
    val until: String?,
    val closeToMe: Boolean,
    val utilities: List<String>
)
