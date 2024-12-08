package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import com.example.pharmacyapp.R
import kotlinx.coroutines.launch

class RegistrationViewModel: ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseModel>>(PendingResult())
    val result: LiveData<Result<ResponseModel>> = _result

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun setUserInfo(
        userInfoModel: UserInfoModel,
        getStringById:(Int) -> String
    ){
        if (
            userInfoModel.firstName.isEmpty() || userInfoModel.firstName.isBlank() ||
            userInfoModel.lastName.isEmpty() || userInfoModel.lastName.isBlank() ||
            userInfoModel.email.isEmpty() || userInfoModel.email.isBlank() ||
            userInfoModel.phoneNumber.isEmpty() || userInfoModel.phoneNumber.isBlank() ||
            userInfoModel.userPassword.isEmpty() || userInfoModel.userPassword.isBlank() ||
            userInfoModel.city.isEmpty() || userInfoModel.city.isBlank()
            ){
            _message.value = getStringById(R.string.enter_the_data)
        }
        else{
            viewModelScope.launch {
                val createUserUseCase = CreateUserUseCase(
                    profileRepository = profileRepositoryImpl,
                    userInfoModel = userInfoModel
                )
                val result = createUserUseCase.execute()
                _result.value = result

            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","RegistrationViewModel onCleared")
    }

}