package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.UserModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [EditUserUseCase] является UseCase для редактирования учетной записи.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userModel] - отридактированная информация пользователя.
 */
class EditUserUseCase(
    private val profileRepository: ProfileRepository,
    private val userModel: UserModel
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.editUserFlow(
            userModel = userModel
        )

        return result
    }

}