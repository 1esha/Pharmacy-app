package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel

class GetCityByUserIdUseCase(
    private val profileRepository: ProfileRepository<ResponseModel, ResponseValueModel<UserModel>, ResponseValueModel<Int>, ResponseValueModel<String>>,
    private val userId: Int
) {

    suspend fun execute(): Result<ResponseValueModel<String>> {
        val result = profileRepository.getCityByUserId(userId = userId)

        return result
    }

}