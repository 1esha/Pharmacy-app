package com.example.domain.favorite.models

import java.io.Serializable

data class FavoriteModel(
    val productId: Int,
    val title: String,
    val productPath: String,
    val price: Double,
    val discount: Double,
    val image: String
): Serializable
