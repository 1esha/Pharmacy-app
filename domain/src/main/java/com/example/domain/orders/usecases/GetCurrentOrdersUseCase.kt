package com.example.domain.orders.usecases

import com.example.domain.Result
import com.example.domain.orders.OrdersRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetCurrentOrdersUseCase] является UseCase для получение списка текущих заказов.
 *
 * Параметры:
 * [userId] - идентификатор пользователя, чей списка текущих заказов будет получен.
 */
class GetCurrentOrdersUseCase(
    private val ordersRepository: OrdersRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result> {
        val result = ordersRepository.getCurrentOrdersFlow(
            userId = userId
        )

        return result
    }

}