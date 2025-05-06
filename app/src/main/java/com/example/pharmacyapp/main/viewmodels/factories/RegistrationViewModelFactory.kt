package com.example.pharmacyapp.main.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.profile.ProfileRepositoryImpl
import com.example.pharmacyapp.R
import com.example.pharmacyapp.main.viewmodels.RegistrationViewModel

class RegistrationViewModelFactory(private val context: Context): ViewModelProvider.Factory {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegistrationViewModel(
            profileRepositoryImpl = profileRepositoryImpl,
            enterTheData = context.getString(R.string.enter_the_data),
            wrongPhoneNumberDialed = context.getString(R.string.wrong_phone_number_dialed)
        ) as T
    }

}