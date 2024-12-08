package com.example.domain.profile

import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.UserInfoModel

interface ProfileRepository<R,V> {

    suspend fun createUser(userInfoModel: UserInfoModel): Result<R>

    suspend fun getUser(logInModel: LogInModel): Result<V>
}