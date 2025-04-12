package com.example.pharmacyapp.tabs.profile.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.orders.OrdersRepositoryImpl
import com.example.pharmacyapp.tabs.profile.viewmodels.BookedGoodsViewModel

class BookedGoodsViewModelFactory: ViewModelProvider.Factory {

    private val ordersRepositoryImpl = OrdersRepositoryImpl()

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BookedGoodsViewModel(
            ordersRepositoryImpl = ordersRepositoryImpl,
            catalogRepositoryImpl = catalogRepositoryImpl
        ) as T
    }

}