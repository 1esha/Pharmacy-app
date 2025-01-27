package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductsViewModel

class ProductsViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProductsViewModel(
            catalogRepository = catalogRepositoryImpl,
            favoriteRepository = favoriteRepositoryImpl ) as T
    }
}