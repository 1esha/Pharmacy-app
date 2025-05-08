package com.example.pharmacyapp.main.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.R
import com.example.pharmacyapp.main.viewmodels.ChangeUserPasswordViewModel

class ChangeUserPasswordViewModelFactory(private val context: Context): ViewModelProvider.Factory {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChangeUserPasswordViewModel(
            profileRepositoryImpl = profileRepositoryImpl,
            enterTheData = context.getString(R.string.enter_the_data),
            invalidPassword = context.getString(R.string.invalid_current_password)
        ) as T
    }

}