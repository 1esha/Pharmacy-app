package com.example.domain.profile.models

import com.example.domain.EncryptionUtils

data class LogInModel(
    val login: String,
    val userPassword: String
) {
    fun isEmpty(): Boolean{
        val logInModel = this
        return logInModel.login.isEmpty() || logInModel.login.isBlank() ||
                logInModel.userPassword.isEmpty() || logInModel.userPassword.isBlank()
    }

    fun encrypt(): LogInModel {
        val encryptionUtils = EncryptionUtils()
        val encryptLogInModel = this.copy(
            login = encryptionUtils.encrypt(text = this.login)
        )

        return encryptLogInModel
    }
}
