package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductInfoViewModel

/**
 * Класс [ProductInfoViewModelFactory] является фабрикой для создания [ProductInfoViewModel].
 */
class ProductInfoViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    private val basketRepositoryImpl = BasketRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ProductInfoViewModel(
            catalogRepositoryImpl = catalogRepositoryImpl,
            favoriteRepositoryImpl = favoriteRepositoryImpl,
            basketRepositoryImpl = basketRepositoryImpl
        ) as T
    }

}