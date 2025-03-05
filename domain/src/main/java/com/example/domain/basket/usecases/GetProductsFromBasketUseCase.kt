package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import com.example.domain.basket.models.BasketModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel

/**
 * Класс [GetProductsFromBasketUseCase] является UseCase для получения списка товаров из корзины пользователя.
 *
 * Параметры:
 * [basketRepository] - класс от куда берется реализация добавлениея товара в корзину;
 * [userId] - идентификатор пользователя в чью корзину будет добавлен товар.
 */
class GetProductsFromBasketUseCase(
    private val basketRepository: BasketRepository<ResponseModel, ResponseValueModel<List<BasketModel>>, ResponseValueModel<List<Int>>>,
    private val userId: Int
) {

    suspend fun execute(): Result<ResponseValueModel<List<BasketModel>>> {
        val result = basketRepository.getProductsFromBasket(userId = userId)

        return result
    }

}