package com.example.data.profile.datasource.remote

import com.example.data.ResultDataSource
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [ProfileRepositoryDataSourceRemote] является репозиторием для работы с данными пользователей в data слое.
 */
interface ProfileRepositoryDataSourceRemote{

    fun createUserFlow(userInfoDataSourceModel: UserInfoDataSourceModel): Flow<ResultDataSource>

    fun getUserFlow(logInDataSourceModel: LogInDataSourceModel): Flow<ResultDataSource>

    fun getUserIdFlow(userInfoDataSourceModel: UserInfoDataSourceModel): Flow<ResultDataSource>

    fun getUserByIdFlow(userId: Int): Flow<ResultDataSource>

    fun editUserFlow(userDataSourceModel: UserDataSourceModel): Flow<ResultDataSource>

    fun deleteUserFlow(userId: Int): Flow<ResultDataSource>

    fun getCityByUserIdFlow(userId: Int): Flow<ResultDataSource>
}