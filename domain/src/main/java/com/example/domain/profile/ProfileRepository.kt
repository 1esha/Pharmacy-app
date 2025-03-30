package com.example.domain.profile

import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [ProfileRepository] является репозиторием для работы с данными пользователей.
 */
interface ProfileRepository{

    fun createUserFlow(userInfoModel: UserInfoModel): Flow<Result>

    fun getUserFlow(logInModel: LogInModel): Flow<Result>

    fun getUserIdFlow(userInfoModel: UserInfoModel): Flow<Result>

    fun getUserByIdFlow(userId: Int): Flow<Result>

    fun editUserFlow(userModel: UserModel): Flow<Result>

    fun deleteUserFlow(userId: Int): Flow<Result>

    fun getCityByUserIdFlow(userId: Int): Flow<Result>
}