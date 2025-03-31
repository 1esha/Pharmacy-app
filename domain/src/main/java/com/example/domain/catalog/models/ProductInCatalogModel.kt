package com.example.domain.catalog.models

data class ProductInCatalogModel(
    val isFavorite: Boolean,
    val productModel: ProductModel,
    val isInBasket: Boolean
)
