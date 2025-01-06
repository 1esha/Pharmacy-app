package com.example.domain.models

data class SelectedPharmacyAddressesModel(
    val isSelected: Boolean = false,
    val pharmacyAddressesModel: PharmacyAddressesModel
)
