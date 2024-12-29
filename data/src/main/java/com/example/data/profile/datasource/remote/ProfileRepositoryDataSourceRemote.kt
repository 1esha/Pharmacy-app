package com.example.data.profile.datasource.remote

import com.example.data.ResultDataSource
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel

interface ProfileRepositoryDataSourceRemote<R,V,I> {

    suspend fun createUser(userInfoDataSourceModel: UserInfoDataSourceModel): ResultDataSource<R>

    suspend fun getUser(logInDataSourceModel: LogInDataSourceModel): ResultDataSource<V>

    suspend fun getUserId(userInfoDataSourceModel: UserInfoDataSourceModel): ResultDataSource<I>

    suspend fun getUserById(userId: Int): ResultDataSource<V>

    suspend fun editUser(userDataSourceModel: UserDataSourceModel): ResultDataSource<R>

    suspend fun deleteUser(userId: Int): ResultDataSource<R>
}