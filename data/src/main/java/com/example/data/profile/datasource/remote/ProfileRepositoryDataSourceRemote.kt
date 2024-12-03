package com.example.data.profile.datasource.remote

import com.example.data.profile.datasource.ResultDataSource
import com.example.data.profile.datasource.models.UserInfoDataSourceModel

interface ProfileRepositoryDataSourceRemote<R,V> {

    suspend fun createUser(userInfoDataSourceModel: UserInfoDataSourceModel): ResultDataSource<R>
}