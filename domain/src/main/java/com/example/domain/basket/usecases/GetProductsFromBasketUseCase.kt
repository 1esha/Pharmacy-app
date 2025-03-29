package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductsFromBasketUseCase] является UseCase для получения списка товаров из корзины пользователя.
 *
 * Параметры:
 * [basketRepository] - интерфейс репозитория;
 * [userId] - идентификатор пользователя из чьей корзины будут получены товаров.
 */
class GetProductsFromBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.getProductsFromBasketFlow(userId = userId)

        return result
    }

}