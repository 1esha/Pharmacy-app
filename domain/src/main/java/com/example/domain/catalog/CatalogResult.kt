package com.example.domain.catalog

import java.lang.Exception

interface CatalogResult {

    fun <T>onSuccessResultListener(value: T, type: String? = null)

    fun onErrorResultListener(exception: Exception, message: String)

    fun onPendingResultListener()

    fun onSuccessfulEvent(type: String, exception: Exception? = null,onSuccessfulEventListener:() -> Unit)

    fun updateUI(flag: String, messageError: String? = null)
}