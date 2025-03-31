package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [DeleteAllFavoriteUseCase] является UseCase для удаления всех товаров из списка избранного.
 *
 * Параметры:
 * [favoriteRepository] - репозиторий с функционалом.
 */
class DeleteAllFavoriteUseCase(private val favoriteRepository: FavoriteRepository) {

    fun execute(): Flow<Result> {
        val result = favoriteRepository.deleteAllFavoriteFlow()

        return result
    }

}