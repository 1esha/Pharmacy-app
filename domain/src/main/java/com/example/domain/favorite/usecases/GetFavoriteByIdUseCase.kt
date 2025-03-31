package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetFavoriteByIdUseCase] является UseCase для получения товара из списка избранного по его идентификатору.
 *
 * Параметры:
 * [favoriteRepository] - репозиторий с функционалом;
 * [productId] - идентификатор товара, который будет получен.
 */
class GetFavoriteByIdUseCase(
    private val favoriteRepository: FavoriteRepository,
    private val productId: Int
) {

    fun execute(): Flow<Result> {
        val result = favoriteRepository.getFavoriteByIdFlow(productId = productId)

        return result
    }

}