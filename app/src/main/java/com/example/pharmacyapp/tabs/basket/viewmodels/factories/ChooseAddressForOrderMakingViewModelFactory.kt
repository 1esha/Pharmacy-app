package com.example.pharmacyapp.tabs.basket.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.tabs.basket.viewmodels.ChooseAddressForOrderMakingViewModel

class ChooseAddressForOrderMakingViewModelFactory: ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChooseAddressForOrderMakingViewModel(
            catalogRepositoryImpl = catalogRepositoryImpl,
            profileRepositoryImpl = profileRepositoryImpl
        ) as T
    }

}