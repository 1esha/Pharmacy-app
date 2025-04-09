package com.example.domain.orders.usecases

import com.example.domain.Result
import com.example.domain.models.NumberProductsModel
import com.example.domain.orders.OrdersRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [CreateOrderUseCase] является UseCase для создания заказа.
 *
 * Параметры:
 * [userId] - идентификатор пользователя для которого оформляется заказ;
 * [addressId] - идентификатор аптеки, где будет получен заказ;
 * [listNumberProductsModel] - список количества товаров в заказе.
 */
class CreateOrderUseCase(
    private val ordersRepository: OrdersRepository,
    private val userId: Int,
    private val addressId: Int,
    private val listNumberProductsModel: List<NumberProductsModel>
) {

    fun execute(): Flow<Result>{
        val result = ordersRepository.createOrderFlow(
            userId = userId,
            addressId = addressId,
            listNumberProductsModel = listNumberProductsModel
        )

        return result
    }

}