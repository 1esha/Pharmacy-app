package com.example.pharmacyapp.tabs.profile.viewmodels.factories

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.pharmacyapp.tabs.profile.viewmodels.FavoriteViewModel

/**
 * Класс [FavoriteViewModelFactory] является фабрикой для создания [FavoriteViewModel].
 */
class FavoriteViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val savedStateHandle = SavedStateHandle()

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    private val basketRepositoryImpl = BasketRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FavoriteViewModel(
            savedStateHandle = savedStateHandle,
            favoriteRepositoryImpl = favoriteRepositoryImpl,
            basketRepositoryImpl = basketRepositoryImpl
        ) as T
    }
}