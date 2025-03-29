package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetIdsProductsFromBasketUseCase] является UseCase для получения списка идентификаторов товаров из корзины пользователя.
 *
 * Параметры:
 * [basketRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя из чьей корзины будут получены идентификаторы товаров.
 */
class GetIdsProductsFromBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.getIdsProductsFromBasketFlow(userId = userId)

        return result
    }

}