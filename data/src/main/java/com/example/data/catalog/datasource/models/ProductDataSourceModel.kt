package com.example.data.catalog.datasource.models

data class ProductDataSourceModel(
    val product_id: Int,
    val title: String,
    val product_path: String,
    val price: Int,
    val discount: Int,
    val product_basic_info: Map<String,String>,
    val product_detailed_info: Map<String,String>,
    val image: String
)
