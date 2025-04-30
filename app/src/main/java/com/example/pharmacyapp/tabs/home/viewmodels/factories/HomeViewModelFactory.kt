package com.example.pharmacyapp.tabs.home.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.advertisements.AdvertisementRepositoryImpl
import com.example.pharmacyapp.tabs.home.viewmodels.HomeViewModel

class HomeViewModelFactory: ViewModelProvider.Factory {

    private val advertisementRepositoryImpl = AdvertisementRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            advertisementRepositoryImpl = advertisementRepositoryImpl
        ) as T
    }

}