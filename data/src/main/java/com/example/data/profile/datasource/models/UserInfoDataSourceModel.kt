package com.example.data.profile.datasource.models

data class UserInfoDataSourceModel(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val userPasswordHashCode: Int,
    val city: String
)
