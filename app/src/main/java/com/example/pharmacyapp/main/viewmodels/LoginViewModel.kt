package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserUseCase
import com.example.pharmacyapp.R
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<UserModel>>>(PendingResult())
    val result: LiveData<Result<ResponseValueModel<UserModel>>> = _result

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun setLogInData(logInModel: LogInModel,getStringById:(Int) -> String){
            if (
                logInModel.login.isEmpty() || logInModel.login.isBlank() ||
                logInModel.userPassword.isEmpty() || logInModel.userPassword.isBlank()
            ){
                _message.value = getStringById(R.string.enter_the_data)
            }
            else{
                viewModelScope.launch {
                    val getUserUseCase = GetUserUseCase(
                        profileRepository = profileRepositoryImpl,
                        logInModel = logInModel
                    )
                    val result = getUserUseCase.execute()
                    _result.value = result
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","LoginViewModel onCleared")
    }
}
