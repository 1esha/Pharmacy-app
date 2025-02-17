package com.example.pharmacyapp.tabs.catalog.viewmodels.factories

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductInfoViewModel

class ProductInfoViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val savedStateHandle = SavedStateHandle()

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return ProductInfoViewModel(
            savedStateHandle = savedStateHandle,
            catalogRepositoryImpl = catalogRepositoryImpl,
            favoriteRepositoryImpl = favoriteRepositoryImpl
        ) as T
    }

}