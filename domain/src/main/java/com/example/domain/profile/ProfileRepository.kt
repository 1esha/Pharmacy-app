package com.example.domain.profile

import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

interface ProfileRepository<R,V,I,S> {

    suspend fun createUser(userInfoModel: UserInfoModel): Result<R>

    suspend fun getUser(logInModel: LogInModel): Result<V>

    suspend fun getUserId(userInfoModel: UserInfoModel): Result<I>

    suspend fun getUserById(userId: Int): Result<V>

    suspend fun editUser(userModel: UserModel): Result<R>

    suspend fun deleteUser(userId: Int): Result<R>

    suspend fun getCityByUserId(userId: Int): Result<S>
}