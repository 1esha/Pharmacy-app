package com.example.data.basket.datasource

import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [BasketRepositoryDataSourceRemote] является репозиторием для работы с корзиной пользователя в data слое.
 */
interface BasketRepositoryDataSourceRemote{

    fun addProductInBasketFlow(userId: Int, productId: Int, numberProducts: Int): Flow<ResultDataSource>

    fun deleteProductFromBasketFlow(userId: Int, productId: Int): Flow<ResultDataSource>

    fun getIdsProductsFromBasketFlow(userId: Int): Flow<ResultDataSource>

    fun getProductsFromBasketFlow(userId: Int): Flow<ResultDataSource>

    fun deleteProductsFromBasketFlow(userId: Int, listIdsProducts: List<Int>): Flow<ResultDataSource>

    fun updateNumberProductsInBasketFlow(userId: Int, productId: Int, numberProducts: Int): Flow<ResultDataSource>

    fun updateNumbersProductsInBasketFlow(userId: Int, listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>): Flow<ResultDataSource>
}