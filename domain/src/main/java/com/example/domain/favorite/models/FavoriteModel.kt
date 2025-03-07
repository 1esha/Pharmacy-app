package com.example.domain.favorite.models

import java.io.Serializable

/**
 * Класс [FavoriteModel] является моделью данных, избранного товара.
 *
 * Параметры:
 * [productId] - идентификатор товара;
 * [title] - заголовок/название товара;
 * [productPath] - путь к товару;
 * [price] - цена;
 * [discount] - скидка;
 * [image] - ссылка на изображение.
 */
data class FavoriteModel(
    val productId: Int,
    val title: String,
    val productPath: String,
    val price: Double,
    val discount: Double,
    val image: String
): Serializable
