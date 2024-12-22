package com.example.data

import com.example.data.profile.datasource.ErrorResultDataSource
import com.example.data.profile.datasource.PendingResultDataSource
import com.example.data.profile.datasource.ResultDataSource
import com.example.data.profile.datasource.SuccessResultDataSource
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

fun UserInfoModel.toUserInfoDataSourceModel(): UserInfoDataSourceModel {

    return UserInfoDataSourceModel(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        userPassword = this.userPassword,
        city = this.city
    )
}

fun <I,O> ResultDataSource<I>.toResult(value:O?):Result<O>{
    return when(this){
        is PendingResultDataSource<I> -> {
            PendingResult<O>()
        }
        is SuccessResultDataSource<I> -> {
            SuccessResult<O>(
                value = value
            )
        }
        is ErrorResultDataSource<I> -> {
            ErrorResult<O>(
                exception = this.exception
            )
        }
    }
}


fun <T>ResultDataSource<T>.asSuccessResultDataSource():SuccessResultDataSource<T>?{
    return if (this is SuccessResultDataSource<T>) this else null
}


fun ResponseDataSourceModel.toResponseModel():ResponseModel{
    return ResponseModel(
        message = this.message,
        status = this.status
    )
}

fun ResponseValueDataSourceModel<UserDataSourceModel>.toResponseValueUserModelModel(): ResponseValueModel<UserModel>{
    return ResponseValueModel(
        value = this.value?.let {
            UserModel(
                userId = it.userId,
                userInfoModel = UserInfoModel(
                    firstName = this.value.userInfoModel.firstName,
                    lastName = this.value.userInfoModel.lastName,
                    email = this.value.userInfoModel.email,
                    phoneNumber = this.value.userInfoModel.phoneNumber,
                    userPassword = this.value.userInfoModel.userPassword,
                    city = this.value.userInfoModel.city,
                )
            )
        },
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<Int>.toResponseValueIntModel(): ResponseValueModel<Int>{
    return ResponseValueModel(
        value = this.value ,
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun LogInModel.toLogInDataSourceModel(): LogInDataSourceModel {
    return LogInDataSourceModel(
        login = this.login,
        userPassword = this.userPassword
    )
}

fun UserModel.toUserDataSourceModel(): UserDataSourceModel{
    return UserDataSourceModel(
        userId = this.userId,
        userInfoModel = this.userInfoModel.toUserInfoDataSourceModel()
    )
}