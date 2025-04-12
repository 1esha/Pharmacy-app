package com.example.data.orders.datasource.models

data class OrderDataSourceModel(
    val orderId: Int,
    val productId: Int,
    val addressId: Int,
    val userId: Int,
    val numberProduct: Int,
    val isActual: Byte,
    val orderDate: String,
    val endDate: String?
)
