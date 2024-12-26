package com.example.data.profile.datasource


sealed class ResultDataSource <T>

class PendingResultDataSource<T>: ResultDataSource<T>()

class SuccessResultDataSource<T>(
    val value: T
): ResultDataSource<T>()

class ErrorResultDataSource<T>(
    val exception: Exception
) : ResultDataSource<T>()