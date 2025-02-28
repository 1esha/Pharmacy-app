package com.example.domain.catalog.models

import java.io.Serializable

data class PharmacyAddressesDetailsModel(
    val pharmacyAddressesModel: PharmacyAddressesModel,
    val latitude: Double,
    val longitude: Double,
    val image: String,
    val modeId: Int
): Serializable
