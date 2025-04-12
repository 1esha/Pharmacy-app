package com.example.domain.orders

import com.example.domain.Result
import com.example.domain.models.NumberProductsModel
import kotlinx.coroutines.flow.Flow

interface OrdersRepository {

    fun createOrderFlow(userId: Int,addressId: Int,listNumberProductsModel: List<NumberProductsModel>): Flow<Result>

    fun getPurchaseHistoryFlow(userId: Int): Flow<Result>

    fun getCurrentOrdersFlow(userId: Int): Flow<Result>
}