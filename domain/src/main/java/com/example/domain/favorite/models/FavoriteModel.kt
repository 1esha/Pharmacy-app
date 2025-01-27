package com.example.domain.favorite.models

data class FavoriteModel(
    val productId: Int,
    val title: String,
    val productPath: String,
    val price: Double,
    val discount: Double,
    val image: String
)
