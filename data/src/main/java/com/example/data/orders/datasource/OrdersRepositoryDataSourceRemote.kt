package com.example.data.orders.datasource

import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import kotlinx.coroutines.flow.Flow

interface OrdersRepositoryDataSourceRemote {

    fun createOrderFlow(userId: Int, addressId: Int, listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>): Flow<ResultDataSource>

    fun getPurchaseHistoryFlow(userId: Int): Flow<ResultDataSource>

    fun getCurrentOrdersFlow(userId: Int): Flow<ResultDataSource>
}