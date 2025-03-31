package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [DeleteByIdUseCase] является UseCase для удаления товара из списка избранного по его идентификатору.
 *
 * Параметры:
 * [favoriteRepository] - репозиторий с функционалом;
 * [productId] - идентификатор удаляемого товара.
 */
class DeleteByIdUseCase(
    private val favoriteRepository: FavoriteRepository,
    private val productId: Int
) {

    fun execute(): Flow<Result> {
        val result = favoriteRepository.deleteByIdFlow(productId = productId)

        return result
    }

}