package com.example.domain.catalog.models

data class ProductAvailabilityModel(
    val productId: Int,
    val addressId: Int,
    val productPath: String,
    val numberProducts: Int,
)
