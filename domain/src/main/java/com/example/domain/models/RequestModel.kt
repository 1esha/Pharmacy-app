package com.example.domain.models

import com.example.domain.Result

data class RequestModel(
    val type: String? = null,
    val result: Result
)
