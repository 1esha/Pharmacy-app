package com.example.domain.models

import com.example.domain.catalog.models.ProductModel
import com.example.domain.orders.models.OrderModel

data class OrderProductModel(
    val orderModel: OrderModel,
    val productModel: ProductModel
)
