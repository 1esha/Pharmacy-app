package com.example.domain.profile.models

data class UserInfoModel(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val userPassword: String,
    val city: String
) {

    fun isEmpty(): Boolean {
        val userInfoModel = this
        return  userInfoModel.firstName.isEmpty() || userInfoModel.firstName.isBlank() ||
                userInfoModel.lastName.isEmpty() || userInfoModel.lastName.isBlank() ||
                userInfoModel.email.isEmpty() || userInfoModel.email.isBlank() ||
                userInfoModel.phoneNumber.isEmpty() || userInfoModel.phoneNumber.isBlank() ||
                userInfoModel.userPassword.isEmpty() || userInfoModel.userPassword.isBlank() ||
                userInfoModel.city.isEmpty() || userInfoModel.city.isBlank()
    }
}
