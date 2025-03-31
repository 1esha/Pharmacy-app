package com.example.domain

/**
 * Интерфейс [ResultProcessing] является контрактом для обработки результатов на экранах.
 */
interface ResultProcessing {

    fun <T>onSuccessResultListener(data: T)

    fun onErrorResultListener(exception: Exception)

    fun onLoadingResultListener()

    fun updateUI(flag: String, messageError: String? = null)

}