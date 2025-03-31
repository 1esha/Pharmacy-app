package com.example.domain.profile.models

data class LogInModel(
    val login: String,
    val userPassword: String
) {
    fun isEmpty(): Boolean{
        val logInModel = this
        return logInModel.login.isEmpty() || logInModel.login.isBlank() ||
                logInModel.userPassword.isEmpty() || logInModel.userPassword.isBlank()
    }
}
