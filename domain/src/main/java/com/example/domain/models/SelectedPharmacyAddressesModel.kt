package com.example.domain.models

import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.catalog.models.ProductAvailabilityModel

data class SelectedPharmacyAddressesModel(
    val isSelected: Boolean = false,
    val pharmacyAddressesModel: PharmacyAddressesModel,
    val productAvailabilityModel: ProductAvailabilityModel
)
