package com.example.domain.models

data class MediatorResultsModel<T>(
    val type: String,
    val result: T
)
