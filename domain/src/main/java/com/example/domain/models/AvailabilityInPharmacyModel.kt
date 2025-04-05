package com.example.domain.models

data class AvailabilityInPharmacyModel(
    val colorInStock: Int,
    val colorOutOfStock: Int,
    val colorWarning: Int,
    val textInStock: String,
    val textOutOfStock: String,
    val textWarning: String
)
