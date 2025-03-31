package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetAllFavoritesUseCase] является UseCase для получения списка со всеми избранными товарами.
 *
 * Параметры:
 * [favoriteRepository] - репозиторий с функционалом.
 */
class GetAllFavoritesUseCase(
    private val favoriteRepository: FavoriteRepository,
) {

    fun execute(): Flow<Result> {
        val result = favoriteRepository.getAllFavoritesFlow()

        return result
    }

}