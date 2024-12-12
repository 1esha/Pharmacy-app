package com.example.domain.profile

import java.lang.Exception

interface ProfileResult {

    var isShow: Boolean

    fun getStringById(id: Int): String

    fun onSuccessResultListener(userId: Int)

    fun onErrorResultListener(exception: Exception)
}