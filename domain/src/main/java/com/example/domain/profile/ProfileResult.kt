package com.example.domain.profile

import java.lang.Exception

interface ProfileResult {

    fun <T>onSuccessResultListener(userId: Int, value: T, type: String? = null)

    fun onErrorResultListener(exception: Exception, message: String)

    fun onPendingResultListener()

    fun onSuccessfulEvent(type: String, exception: Exception? = null,onSuccessfulEventListener:() -> Unit)

    fun updateUI(flag: String, messageError: String? = null)
}