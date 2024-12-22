package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel

class EditUserUseCase(
    private val profileRepository: ProfileRepository<ResponseModel, ResponseValueModel<UserModel>, ResponseValueModel<Int>>,
    private val userModel: UserModel
) {

    suspend fun execute(): Result<ResponseModel>{
        val result = profileRepository.editUser(
            userModel = userModel
        )

        return result
    }

}