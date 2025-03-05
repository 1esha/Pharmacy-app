package com.example.data.catalog.datasource.models

data class ProductDataSourceModel(
    val productId: Int,
    val title: String,
    val productPath: String,
    val price: Double,
    val discount: Double,
    val productBasicInfo: List<Map<String,String>>,
    val productDetailedInfo: List<Map<String,String>>,
    val image: String
)
