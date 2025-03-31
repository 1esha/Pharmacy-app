package com.example.domain.models

import com.example.domain.catalog.models.PharmacyAddressesModel

data class SelectedPharmacyAddressesModel(
    val isSelected: Boolean = false,
    val pharmacyAddressesModel: PharmacyAddressesModel
)
