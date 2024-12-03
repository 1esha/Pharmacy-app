package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel

class CreateUserUseCase<T>(
    private val profileRepository: ProfileRepository<ResponseModel,ResponseValueModel<T>>,
    private val userInfoModel: UserInfoModel
) {

    suspend fun execute():Result<ResponseModel>{
        val result = profileRepository.createUser(
            userInfoModel = userInfoModel
        )
        return result
    }

}