package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [AddProductInBasketUseCase] является UseCase для добавлениея товара в корзину.
 *
 * Параметры:
 * [basketRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя в чью корзину будет добавлен товар;
 * [productId] - идентификатор товара для добавления;
 * [numberProducts] - количество товра, которое будет добавлено в коризну.
 */
class AddProductInBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int,
    private val productId: Int,
    private val numberProducts: Int
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.addProductInBasketFlow(
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )

        return result
    }

}