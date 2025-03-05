package com.example.data.basket.datasource.models

import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.domain.basket.models.BasketModel

/**
 * Класс [BasketModel] является моделью корзины пользователя для работы в data слое.
 *
 * Параметры:
 * [productDataSourceModel] - товар в корзине;
 * [numberProducts] - количество товара в корзине.
 */
data class BasketDataSourceModel(
    val productDataSourceModel: ProductDataSourceModel,
    val numberProducts: Int
)
