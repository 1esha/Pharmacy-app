package com.example.data.catalog.datasource.models

data class PharmacyAddressesDetailsDataSourceModel(
    val pharmacyAddressesDataSourceModel: PharmacyAddressesDataSourceModel,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val modeId: Int
)
