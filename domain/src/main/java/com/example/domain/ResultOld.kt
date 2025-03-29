package com.example.domain

sealed class ResultOld <T>

class PendingResult<T>: ResultOld<T>()

class SuccessResult<T>(
    val value: T?
): ResultOld<T>()

class ErrorResult<T>(
    val exception: Exception
) : ResultOld<T>()

fun <T>ResultOld<T>.asSuccessResult(): SuccessResult<T>?{
    return if (this is SuccessResult) this else null
}