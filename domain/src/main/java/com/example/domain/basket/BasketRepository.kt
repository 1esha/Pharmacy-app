package com.example.domain.basket

import com.example.domain.Result

/**
 * Интерфейс [BasketRepository] является репозиторием для работы с корзиной пользователя.
 */
interface BasketRepository<R,B,I> {

    suspend fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int): Result<R>

    suspend fun deleteProductFromBasket(userId: Int, productId: Int): Result<R>

    suspend fun getIdsProductsFromBasket(userId: Int): Result<I>

    suspend fun getProductsFromBasket(userId: Int): Result<B>

    suspend fun updateNumberProductsInBasket(userId: Int, productId: Int, numberProducts: Int): Result<R>
}