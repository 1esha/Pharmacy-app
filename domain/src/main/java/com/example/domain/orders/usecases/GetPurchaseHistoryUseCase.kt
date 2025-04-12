package com.example.domain.orders.usecases

import com.example.domain.Result
import com.example.domain.orders.OrdersRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetPurchaseHistoryUseCase] является UseCase для получение списка истории покупок.
 *
 * Параметры:
 * [userId] - идентификатор пользователя, чья история покупок будет получена.
 */
class GetPurchaseHistoryUseCase(
    private val ordersRepository: OrdersRepository,
    private val userId: Int
) {

    fun execute(): Flow<Result>{
        val result = ordersRepository.getPurchaseHistoryFlow(
            userId = userId
        )

        return result
    }

}