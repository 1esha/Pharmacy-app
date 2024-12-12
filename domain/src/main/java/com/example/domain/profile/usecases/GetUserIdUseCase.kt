package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

class GetUserIdUseCase(
    private val profileRepository: ProfileRepository<ResponseModel, ResponseValueModel<UserModel>, ResponseValueModel<Int>>,
    private val userInfoModel: UserInfoModel
) {

    suspend fun execute(): Result<ResponseValueModel<Int>>{
        val result = profileRepository.getUserId(
            userInfoModel = userInfoModel
        )
        return result
    }

}