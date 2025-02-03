package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.PharmacyAddressesViewModel

class PharmacyAddressesViewModelFactory : ViewModelProvider.Factory {

    private val savedStateHandle = SavedStateHandle()

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PharmacyAddressesViewModel(
            savedStateHandle = savedStateHandle,
            catalogRepositoryImpl = catalogRepositoryImpl
        ) as T
    }
}