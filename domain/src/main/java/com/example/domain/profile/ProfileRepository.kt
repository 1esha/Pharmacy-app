package com.example.domain.profile

import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.UserInfoModel

interface ProfileRepository<R,V,I> {

    suspend fun createUser(userInfoModel: UserInfoModel): Result<R>

    suspend fun getUser(logInModel: LogInModel): Result<V>

    suspend fun getUserId(userInfoModel: UserInfoModel): Result<I>

    suspend fun getUserById(userId: Int): Result<V>
}