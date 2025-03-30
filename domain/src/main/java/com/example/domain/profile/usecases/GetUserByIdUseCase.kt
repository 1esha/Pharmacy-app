package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetUserByIdUseCase] является UseCase для получения данных пользователя по идентификатору.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя.
 */
class GetUserByIdUseCase(
    private val profileRepository: ProfileRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.getUserByIdFlow(userId = userId)

        return result
    }

}