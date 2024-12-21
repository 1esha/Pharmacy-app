package com.example.domain

sealed class Result <T>

class PendingResult<T>: Result<T>()

class SuccessResult<T>(
    val value: T?
): Result<T>()

class ErrorResult<T>(
    val exception: Exception
) : Result<T>()

fun <T>Result<T>.asSuccessResult(): SuccessResult<T>?{
    return if (this is SuccessResult) this else null
}