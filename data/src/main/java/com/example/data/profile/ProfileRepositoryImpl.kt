package com.example.data.profile

import android.util.Log
import com.example.data.asSuccessResultDataSource
import com.example.data.profile.datasource.SuccessResultDataSource
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.remote.ProfileRepositoryDataSourceRemoteImpl
import com.example.data.toResponseModel
import com.example.data.toResult
import com.example.data.toUserInfoDataSourceModel
import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel

class ProfileRepositoryImpl<T> : ProfileRepository<ResponseModel, ResponseValueModel<T>> {

    private val profileRepositoryDataSourceRemote = ProfileRepositoryDataSourceRemoteImpl<T>()

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

}