package com.example.domain

import kotlin.Exception

sealed class Result{

    class Loading: Result()

    data class Success<T>(val data: T): Result()

    data class Error(val exception: Exception): Result()

}

fun Result.asSuccess(): Result.Success<*>?{
    return if (this is Result.Success<*>) this else null
}

fun Result.asError(): Result.Error?{
    return if (this is Result.Error) this else null
}