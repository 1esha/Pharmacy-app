package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import com.example.domain.basket.models.BasketModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel

/**
 * Класс [UpdateNumberProductsInBasketUseCase] является UseCase для обновления количества товара в корзину.
 *
 * Параметры:
 * [basketRepository] - класс от куда берется реализация добавлениея товара в корзину;
 * [userId] - идентификатор пользователя в чью корзину будет добавлен товар;
 * [productId] - идентификатор товара для добавления;
 * [numberProducts] - новое количество товара в корзине.
 */
class UpdateNumberProductsInBasketUseCase(
    private val basketRepository: BasketRepository<ResponseModel, ResponseValueModel<List<BasketModel>>, ResponseValueModel<List<Int>>>,
    private val userId: Int,
    private val productId: Int,
    private val numberProducts: Int
) {

    suspend fun execute(): Result<ResponseModel> {
        val result = basketRepository.updateNumberProductsInBasket(
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )

        return result
    }

}