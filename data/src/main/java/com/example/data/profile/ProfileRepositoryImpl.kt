package com.example.data.profile

import android.util.Log
import com.example.data.asSuccessResultDataSource
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.remote.ProfileRepositoryDataSourceRemoteImpl
import com.example.data.toLogInDataSourceModel
import com.example.data.toResponseModel
import com.example.data.toResponseValueModel
import com.example.data.toResult
import com.example.data.toUserInfoDataSourceModel
import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

class ProfileRepositoryImpl : ProfileRepository<ResponseModel, ResponseValueModel<UserModel>> {

    private val profileRepositoryDataSourceRemote = ProfileRepositoryDataSourceRemoteImpl()

    override suspend fun createUser(userInfoModel: UserInfoModel): Result<ResponseModel> {
        val userInfoDataSourceModel = userInfoModel.toUserInfoDataSourceModel()
        val resultDataSource = profileRepositoryDataSourceRemote.createUser(
            userInfoDataSourceModel = userInfoDataSourceModel
        )
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult<ResponseDataSourceModel,ResponseModel>(
            value = value?.toResponseModel()
        )

        return result
    }

    override suspend fun getUser(logInModel: LogInModel): Result<ResponseValueModel<UserModel>> {
        val logInDataSourceModel = logInModel.toLogInDataSourceModel()
        val resultDataSource = profileRepositoryDataSourceRemote.getUser(
            logInDataSourceModel = logInDataSourceModel
        )
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult<ResponseValueDataSourceModel<UserDataSourceModel>,ResponseValueModel<UserModel>>(
            value = value?.toResponseValueModel()
        )

        return result
    }

}