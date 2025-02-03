package com.example.pharmacyapp.tabs.profile.viewmodels.factories

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.tabs.profile.viewmodels.AuthorizedUserViewModel

class AuthorizedUserViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val favoriteRepositoryImpl = FavoriteRepositoryImpl(context = context)

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val savedStateHandle = SavedStateHandle()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AuthorizedUserViewModel(
            savedStateHandle = savedStateHandle,
            favoriteRepositoryImpl = favoriteRepositoryImpl,
            profileRepositoryImpl = profileRepositoryImpl) as T
    }

}