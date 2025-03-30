package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.UserInfoModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [CreateUserUseCase] является UseCase для создания новой учетной записи.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userInfoModel] - данные создаваемого пользователя.
 */
class CreateUserUseCase(
    private val profileRepository: ProfileRepository,
    private val userInfoModel: UserInfoModel
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.createUserFlow(
            userInfoModel = userInfoModel
        )

        return result
    }

}