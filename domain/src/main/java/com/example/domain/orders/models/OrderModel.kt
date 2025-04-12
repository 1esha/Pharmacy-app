package com.example.domain.orders.models

data class OrderModel(
    val orderId: Int,
    val productId: Int,
    val addressId: Int,
    val userId: Int,
    val numberProduct: Int,
    val isActual: Boolean,
    val orderDate: String,
    val endDate: String?
)
