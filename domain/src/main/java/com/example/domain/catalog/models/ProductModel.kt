package com.example.domain.catalog.models

data class ProductModel(
    val productId: Int,
    val title: String,
    val productPath: String,
    val price: Int,
    val discount: Int,
    val productBasicInfo: Map<String,String>,
    val productDetailedInfo: Map<String,String>,
    val image: String
)
