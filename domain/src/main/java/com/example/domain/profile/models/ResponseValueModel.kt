package com.example.domain.profile.models

data class ResponseValueModel<T>(
    val value: T,
    val responseModel: ResponseModel
)
