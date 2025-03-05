package com.example.domain.basket.models

import com.example.domain.catalog.models.ProductModel

/**
 * Класс [BasketModel] является моделью корзины пользователя.
 *
 * Параметры:
 * [productModel] - товар в корзине;
 * [numberProducts] - количество товара в корзине.
 */
data class BasketModel(
    val productModel: ProductModel,
    val numberProducts: Int
)
