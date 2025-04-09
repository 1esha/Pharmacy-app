package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import com.example.domain.models.NumberProductsModel
import kotlinx.coroutines.flow.Flow

/**
 * Класс [UpdateNumbersProductsInBasketUseCase] является UseCase для обновления количества товаров в корзине.
 *
 * Параметры:
 * [basketRepository] - интерфейс репозитория;
 * [userId] - идентификатор пользователя в чьей корзине будет обнавлено количество товаров;
 * [listNumberProductsModel] - список с новым количеством товаров.
 */
class UpdateNumbersProductsInBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int,
    private val listNumberProductsModel: List<NumberProductsModel>,
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.updateNumbersProductsInBasketFlow(
            userId = userId,
            listNumberProductsModel = listNumberProductsModel
        )

        return result
    }

}