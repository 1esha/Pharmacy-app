package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [DeleteProductFromBasketUseCase] является UseCase для удаления товара из корзины.
 *
 * Параметры:
 * [basketRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя из чьей корзины будет удален товар;
 * [productId] - идентификатор товара для удаления.
 */
class DeleteProductFromBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int,
    private val productId: Int
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.deleteProductFromBasketFlow(
            userId = userId,
            productId = productId
        )

        return result
    }

}