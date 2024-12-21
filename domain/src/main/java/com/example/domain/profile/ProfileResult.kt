package com.example.domain.profile

import java.lang.Exception

interface ProfileResult<T> {

    fun onSuccessResultListener(userId: Int, value: T)

    fun onErrorResultListener(exception: Exception, message: String)

    fun onPendingResult()
}