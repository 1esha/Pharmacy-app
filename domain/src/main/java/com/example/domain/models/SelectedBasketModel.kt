package com.example.domain.models

import com.example.domain.basket.models.BasketModel

data class SelectedBasketModel(
    val isSelect: Boolean = true,
    val basketModel: BasketModel
)
