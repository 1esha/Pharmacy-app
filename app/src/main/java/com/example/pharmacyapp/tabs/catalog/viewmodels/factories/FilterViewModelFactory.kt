package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.FilterViewModel

class FilterViewModelFactory : ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FilterViewModel(catalogRepository = catalogRepositoryImpl) as T
    }
}