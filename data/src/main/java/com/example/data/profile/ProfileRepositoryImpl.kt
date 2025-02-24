package com.example.data.profile

import com.example.data.asSuccessResultDataSource
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.remote.ProfileRepositoryDataSourceRemoteImpl
import com.example.data.toLogInDataSourceModel
import com.example.data.toResponseModel
import com.example.data.toResponseValueIntModel
import com.example.data.toResponseValueStringModel
import com.example.data.toResponseValueUserModelModel
import com.example.data.toResult
import com.example.data.toUserDataSourceModel
import com.example.data.toUserInfoDataSourceModel
import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

class ProfileRepositoryImpl : ProfileRepository<
        ResponseModel,
        ResponseValueModel<UserModel>,
        ResponseValueModel<Int>,
        ResponseValueModel<String>> {

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
            value = value?.toResponseValueUserModelModel()
        )

        return result
    }

    override suspend fun getUserId(userInfoModel: UserInfoModel): Result<ResponseValueModel<Int>> {
        val userInfoDataSourceModel = userInfoModel.toUserInfoDataSourceModel()
        val resultDataSource = profileRepositoryDataSourceRemote.getUserId(
            userInfoDataSourceModel = userInfoDataSourceModel
        )
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(
            value = value?.toResponseValueIntModel()
        )

        return result
    }

    override suspend fun getUserById(userId: Int): Result<ResponseValueModel<UserModel>> {
        val resultDataSourceModel = profileRepositoryDataSourceRemote.getUserById(userId = userId)
        val value = resultDataSourceModel.asSuccessResultDataSource()?.value
        val result = resultDataSourceModel.toResult<ResponseValueDataSourceModel<UserDataSourceModel>,ResponseValueModel<UserModel>>(
            value = value?.toResponseValueUserModelModel()
        )

        return result
    }

    override suspend fun editUser(userModel: UserModel): Result<ResponseModel> {
        val userDataSourceModel = userModel.toUserDataSourceModel()
        val resultDataSource = profileRepositoryDataSourceRemote.editUser(userDataSourceModel = userDataSourceModel)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(
            value = value?.toResponseModel()
        )

        return result
    }

    override suspend fun deleteUser(userId: Int): Result<ResponseModel> {
        val resultDataSourceModel = profileRepositoryDataSourceRemote.deleteUser(
            userId = userId
        )
        val value = resultDataSourceModel.asSuccessResultDataSource()?.value
        val result = resultDataSourceModel.toResult(
            value = value?.toResponseModel()
        )

        return result
    }

    override suspend fun getCityByUserId(userId: Int): Result<ResponseValueModel<String>> {
        val responseValueDataSourceModel = profileRepositoryDataSourceRemote.getCityByUserId(userId = userId)
        val value = responseValueDataSourceModel.asSuccessResultDataSource()?.value
        val result = responseValueDataSourceModel.toResult(
            value = value?.toResponseValueStringModel()
        )

        return result
    }

}