package com.example.data.basket.datasource

import com.example.data.ResultDataSource

/**
 * Интерфейс [BasketRepositoryDataSourceRemote] является репозиторием для работы с корзиной пользователя в data слое.
 */
interface BasketRepositoryDataSourceRemote<R,B,I> {

    suspend fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int): ResultDataSource<R>

    suspend fun deleteProductFromBasket(userId: Int, productId: Int): ResultDataSource<R>

    suspend fun getIdsProductsFromBasket(userId: Int): ResultDataSource<I>

    suspend fun getProductsFromBasket(userId: Int): ResultDataSource<B>

    suspend fun updateNumberProductsInBasket(userId: Int, productId: Int, numberProducts: Int): ResultDataSource<R>
}