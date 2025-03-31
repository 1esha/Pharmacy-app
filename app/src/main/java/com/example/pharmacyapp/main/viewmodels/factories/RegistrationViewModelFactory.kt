package com.example.pharmacyapp.main.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel

class RegistrationViewModelFactory: ViewModelProvider.Factory {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegistrationViewModel(
            profileRepositoryImpl = profileRepositoryImpl
        ) as T
    }

}