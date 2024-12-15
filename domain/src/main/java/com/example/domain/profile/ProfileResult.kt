package com.example.domain.profile

import java.lang.Exception

interface ProfileResult<T> {

    var isShow: Boolean

    fun onSuccessResultListener(userId: Int, value: T)

    fun onErrorResultListener(exception: Exception)

    fun onPendingResult()
}