package com.example.pharmacyapp.tabs.basket.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.basket.BasketRepositoryImpl
import com.example.pharmacyapp.tabs.basket.viewmodels.BasketViewModel

class BasketViewModelFactory: ViewModelProvider.Factory {

    private val basketRepositoryImpl = BasketRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BasketViewModel(
            basketRepositoryImpl = basketRepositoryImpl
        ) as T
    }

}