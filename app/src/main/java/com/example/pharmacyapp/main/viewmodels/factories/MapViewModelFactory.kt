package com.example.pharmacyapp.main.viewmodels.factories

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.main.viewmodels.MapViewModel

/**
 * Класс [MapViewModelFactory] является фабрикой для создания [MapViewModel].
 */
class MapViewModelFactory(): ViewModelProvider.Factory {

    private val savedStateHandle = SavedStateHandle()

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapViewModel(
            savedStateHandle = savedStateHandle,
            profileRepositoryImpl = profileRepositoryImpl,
            catalogRepositoryImpl = catalogRepositoryImpl
        ) as T
    }

}