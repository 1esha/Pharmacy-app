package com.example.data


sealed class ResultDataSourceOld <T>

class PendingResultDataSource<T>: ResultDataSourceOld<T>()

class SuccessResultDataSource<T>(
    val value: T
): ResultDataSourceOld<T>()

class ErrorResultDataSource<T>(
    val exception: Exception
) : ResultDataSourceOld<T>()