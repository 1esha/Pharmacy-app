package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import com.example.domain.profile.usecases.GetUserIdUseCase
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.launch

class RegistrationViewModel: ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseModel>>(PendingResult())
    val result: LiveData<Result<ResponseModel>> = _result

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _userId = MutableLiveData<Int>(UNAUTHORIZED_USER)
    val userId: LiveData<Int> = _userId

    fun createUser(
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

                val getUserIdUseCase = GetUserIdUseCase(
                    profileRepository = profileRepositoryImpl,
                    userInfoModel = userInfoModel
                )

                val resultCreateUser = createUserUseCase.execute()
                val resultGetUserId = getUserIdUseCase.execute()

                when(resultGetUserId){
                    is SuccessResult -> {
                        _userId.value = resultGetUserId.value?.value?: UNAUTHORIZED_USER
                        _result.value = resultCreateUser
                    }
                    is ErrorResult -> {
                        _message.value = getStringById(R.string.error_in_getting_the_id)
                    }
                    else -> {}
                }

            }
        }

    }


    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","RegistrationViewModel onCleared")
    }

}