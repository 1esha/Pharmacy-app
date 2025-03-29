package com.example.domain.basket

import com.example.domain.Result
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [BasketRepository] является репозиторием для работы с корзиной пользователя.
 */
interface BasketRepository {

    fun addProductInBasketFlow(userId: Int, productId: Int, numberProducts: Int): Flow<Result>

    fun deleteProductFromBasketFlow(userId: Int, productId: Int): Flow<Result>

    fun getProductsFromBasketFlow(userId: Int): Flow<Result>

    fun getIdsProductsFromBasketFlow(userId: Int): Flow<Result>

    fun deleteProductsFromBasketFlow(userId: Int, listIdsProducts: List<Int>): Flow<Result>

    fun updateNumberProductsInBasketFlow(userId: Int, productId: Int, numberProducts: Int): Flow<Result>
}