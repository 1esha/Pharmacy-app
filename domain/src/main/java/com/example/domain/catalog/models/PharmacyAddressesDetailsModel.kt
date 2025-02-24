package com.example.domain.catalog.models

data class PharmacyAddressesDetailsModel(
    val pharmacyAddressesModel: PharmacyAddressesModel,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val modeId: Int
)
