package com.example.data.catalog.datasource.models

data class ProductDataSourceModel(
    val product_id: Int,
    val title: String,
    val product_path: String,
    val price: Double,
    val discount: Double,
    val product_basic_info: List<Map<String,String>>,
    val product_detailed_info: List<Map<String,String>>,
    val image: String
)
