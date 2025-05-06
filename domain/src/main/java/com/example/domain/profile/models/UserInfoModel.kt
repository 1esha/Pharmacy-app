package com.example.domain.profile.models

import com.example.domain.EncryptionUtils

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

    fun encrypt(): UserInfoModel {
        val encryptionUtils = EncryptionUtils()
        val encryptUserInfoModel = this.copy(
            firstName = encryptionUtils.encrypt(text = this.firstName),
            lastName = encryptionUtils.encrypt(text = this.lastName),
            email = encryptionUtils.encrypt(text = this.email),
            phoneNumber = encryptionUtils.encrypt(text = this.phoneNumber),
            city = encryptionUtils.encrypt(text = this.city)
        )

        return encryptUserInfoModel
    }

    fun decrypt(): UserInfoModel {
        val encryptionUtils = EncryptionUtils()
        val decryptUserInfoModel = this.copy(
            firstName = encryptionUtils.decrypt(text = this.firstName),
            lastName = encryptionUtils.decrypt(text = this.lastName),
            email = encryptionUtils.decrypt(text = this.email),
            phoneNumber = encryptionUtils.decrypt(text = this.phoneNumber),
            city = encryptionUtils.decrypt(text = this.city)
        )

        return decryptUserInfoModel
    }
}
