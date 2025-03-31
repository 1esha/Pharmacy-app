package com.example.pharmacyapp.main.viewmodels.factories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.main.viewmodels.EditViewModel

class EditViewModelFactory: ViewModelProvider.Factory {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditViewModel(
            profileRepositoryImpl = profileRepositoryImpl
        ) as T
    }

}