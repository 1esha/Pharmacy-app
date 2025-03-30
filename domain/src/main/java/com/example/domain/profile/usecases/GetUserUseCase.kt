package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.LogInModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetUserUseCase] является UseCase для получения данных пользователя по логину и паролю.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [logInModel] - модель данных с логином и паролем.
 */
class GetUserUseCase(
    private val profileRepository: ProfileRepository,
    private val logInModel: LogInModel
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.getUserFlow(
            logInModel = logInModel
        )
        return result
    }

}