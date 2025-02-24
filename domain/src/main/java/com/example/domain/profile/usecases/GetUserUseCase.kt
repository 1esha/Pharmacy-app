package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel

class GetUserUseCase(
    private val profileRepository: ProfileRepository<ResponseModel, ResponseValueModel<UserModel>,ResponseValueModel<Int>,ResponseValueModel<String>>,
    private val logInModel: LogInModel
) {

    suspend fun execute():Result<ResponseValueModel<UserModel>>{
        val result = profileRepository.getUser(
            logInModel = logInModel
        )
        return result
    }

}