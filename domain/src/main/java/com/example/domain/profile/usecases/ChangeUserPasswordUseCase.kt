package com.example.domain.profile.usecases

import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [ChangeUserPasswordUseCase] является UseCase для изменения пароля пользователя.
 *
 * Параметры:
 * [profileRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя у которого будет изменен пароль;
 * [oldUserPassword] - старый пароль пользователя;
 * [newUserPassword] - новый пароль пользователя.
 */
class ChangeUserPasswordUseCase(
    private val profileRepository: ProfileRepository,
    private val userId: Int,
    private val oldUserPassword: String,
    private val newUserPassword: String
) {

    fun execute(): Flow<Result> {
        val result = profileRepository.changeUserPasswordFlow(
            userId = userId,
            oldUserPassword = oldUserPassword.hashCode(),
            newUserPassword = newUserPassword.hashCode()
        )

        return result
    }

}