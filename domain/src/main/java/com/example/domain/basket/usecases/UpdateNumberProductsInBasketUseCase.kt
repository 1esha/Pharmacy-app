package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [UpdateNumberProductsInBasketUseCase] является UseCase для обновления количества товара в корзину.
 *
 * Параметры:
 * [basketRepository] - интерфейс репозитория;
 * [userId] - идентификатор пользователя в чьей корзине будет обнавлено количество товара
 * [productId] - идентификатор товара обновляемого товара;
 * [numberProducts] - новое количество товара в корзине.
 */
class UpdateNumberProductsInBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int,
    private val productId: Int,
    private val numberProducts: Int
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.updateNumberProductsInBasketFlow(
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )

        return result
    }

}