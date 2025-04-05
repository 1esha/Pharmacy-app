package com.example.domain.catalog.models

import java.io.Serializable

data class PharmacyAddressesModel(
    val addressId: Int,
    val address: String,
    val city: String
): Serializable
