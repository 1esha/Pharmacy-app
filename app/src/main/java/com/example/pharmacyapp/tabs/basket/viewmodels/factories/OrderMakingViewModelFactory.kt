package com.example.pharmacyapp.tabs.basket.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.orders.OrdersRepositoryImpl
import com.example.pharmacyapp.tabs.basket.viewmodels.OrderMakingViewModel

class OrderMakingViewModelFactory: ViewModelProvider.Factory {

    private val basketRepositoryImpl = BasketRepositoryImpl()

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val ordersRepositoryImpl = OrdersRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OrderMakingViewModel(
            basketRepositoryImpl = basketRepositoryImpl,
            catalogRepositoryImpl = catalogRepositoryImpl,
            ordersRepositoryImpl = ordersRepositoryImpl
        ) as T
    }

}