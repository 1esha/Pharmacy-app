package com.example.pharmacyapp.tabs.profile.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.pharmacyapp.tabs.profile.viewmodels.AuthorizedUserViewModel

class AuthorizedUserViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthorizedUserViewModel(favoriteRepository = favoriteRepositoryImpl) as T
    }

}