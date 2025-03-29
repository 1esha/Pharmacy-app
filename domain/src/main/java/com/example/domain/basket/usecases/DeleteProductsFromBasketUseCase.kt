package com.example.domain.basket.usecases

import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [DeleteProductsFromBasketUseCase] является UseCase для удаления нескольких товаров из корзины.
 *
 * Параметры:
 * [basketRepository] - репозиторий с функционалом;
 * [userId] - идентификатор пользователя из чьей коризины будут удалены товары;
 * [listIdsProducts] - список идентификаторов товаров для удаления.
 */
class DeleteProductsFromBasketUseCase(
    private val basketRepository: BasketRepository,
    private val userId: Int,
    private val listIdsProducts: List<Int>
) {

    fun execute(): Flow<Result> {
        val result = basketRepository.deleteProductsFromBasketFlow(
            userId = userId,
            listIdsProducts = listIdsProducts
        )

        return result
    }

}