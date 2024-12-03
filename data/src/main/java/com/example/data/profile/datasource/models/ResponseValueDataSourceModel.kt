package com.example.data.profile.datasource.models

data class ResponseValueDataSourceModel<T>(
    val value: T,
    val responseDataSourceModel: ResponseDataSourceModel
)
