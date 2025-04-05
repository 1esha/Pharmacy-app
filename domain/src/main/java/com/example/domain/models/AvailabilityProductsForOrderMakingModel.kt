package com.example.domain.models

data class AvailabilityProductsForOrderMakingModel(
    val addressId: Int,
    val address: String,
    val city: String,
    val availableQuantity: Int
)
