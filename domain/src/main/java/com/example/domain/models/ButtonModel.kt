package com.example.domain.models

/**
 * Класс [ButtonModel] является моделью для кнопки добавления/удаления товара из корзины.
 * Когда товар не в корзине и его можно добавить в корзину используются основные цвета и текст,
 * когда товар уже находиться в корзине, то используются вторичные цвета и текст.
 *
 * Параметры:
 * [colorPrimary] - основной цвет для фона;
 * [colorOnPrimary] - цвет на основном фоне;
 * [colorSecondaryContainer] - вторичный цвет фона;
 * [colorOnSecondaryContainer] - цвет на вторичном фоне;
 * [textPrimary] - текст для основного фона;
 * [textSecondary] - текст для вторичного фона.
 */
data class ButtonModel(
    val colorPrimary: Int,
    val colorOnPrimary: Int,
    val colorSecondaryContainer: Int,
    val colorOnSecondaryContainer: Int,
    val textPrimary: String,
    val textSecondary: String
)
