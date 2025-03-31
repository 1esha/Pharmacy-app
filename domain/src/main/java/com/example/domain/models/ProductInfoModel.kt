package com.example.domain.models

data class ProductInfoModel(
    val image: String,
    val title: String,
    val textOriginalPrice: String,
    val textDiscount: String,
    val textPrice: String,
    val textPriceClub: String,
    val isDiscount: Boolean
)