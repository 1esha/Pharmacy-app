package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [DeleteUserUseCase] является UseCase для удаления учетной записи.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя аккаунт которого будет удален.
 */
class DeleteUserUseCase(
    private val profileRepository: ProfileRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.deleteUserFlow(
            userId = userId
        )

        return result
    }

}