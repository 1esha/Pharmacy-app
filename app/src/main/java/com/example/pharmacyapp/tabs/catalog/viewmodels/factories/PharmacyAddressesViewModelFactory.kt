package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.PharmacyAddressesViewModel

class PharmacyAddressesViewModelFactory : ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PharmacyAddressesViewModel(catalogRepository = catalogRepositoryImpl) as T
    }
}