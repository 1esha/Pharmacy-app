package com.example.data.basket.datasource.models


/**
 * Класс [DeleteProductsFromBasketDataSourceModel] является моделью для удаления нескольких товаров из корзины в data слое.
 *
 * Параметры:
 * [userId] - идентификатор пользователя из чьей коризины будут удалены товары;
 * [listIdsProducts] - список идентификаторов товаров для удаления.
 */
data class DeleteProductsFromBasketDataSourceModel(
    val userId: Int,
    val listIdsProducts: List<Int>
)
