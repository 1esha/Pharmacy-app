package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetCityByUserIdUseCase] является UseCase для получения города пользователя.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя.
 */
class GetCityByUserIdUseCase(
    private val profileRepository: ProfileRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.getCityByUserIdFlow(userId = userId)

        return result
    }

}