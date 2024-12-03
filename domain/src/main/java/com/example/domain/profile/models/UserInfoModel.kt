package com.example.domain.profile.models

data class UserInfoModel(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val userPassword: String,
    val city: String
)
