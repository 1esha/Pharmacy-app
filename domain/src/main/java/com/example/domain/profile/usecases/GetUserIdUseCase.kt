package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.UserInfoModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetUserByIdUseCase] является UseCase для получения идентификатора пользователя по его зашифрованным данным.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userInfoModel] - данные пользователя.
 */
class GetUserIdUseCase(
    private val profileRepository: ProfileRepository,
    private val userInfoModel: UserInfoModel
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.getUserIdFlow(
            userInfoModel = userInfoModel.encrypt()
        )
        return result
    }

}