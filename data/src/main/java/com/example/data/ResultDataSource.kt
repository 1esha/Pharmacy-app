package com.example.data

import kotlin.Exception

sealed class ResultDataSource{

    class Loading: ResultDataSource()

    data class Success<T>(val data: T): ResultDataSource()

    data class Error(val exception: Exception): ResultDataSource()

}

fun ResultDataSource.asSuccess(): ResultDataSource.Success<*>?{
    return if (this is ResultDataSource.Success<*>) this else null
}

fun ResultDataSource.asError(): ResultDataSource.Error?{
    return if (this is ResultDataSource.Error) this else null
}