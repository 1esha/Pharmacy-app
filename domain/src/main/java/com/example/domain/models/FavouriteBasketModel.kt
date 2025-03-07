package com.example.domain.models

import com.example.domain.favorite.models.FavoriteModel

/**
 * Класс [FavouriteBasketModel] является моделью данных для элемента списка избранных товаров.
 *
 * Параметры:
 * [favoriteModel] - информация об избранном товаре;
 * [isInBasket] - значение находится ли товар в корзине или нет.
 */
data class FavouriteBasketModel(
    val favoriteModel: FavoriteModel,
    val isInBasket: Boolean
)
