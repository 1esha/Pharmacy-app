package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DataEntryError
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserUseCase
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<UserModel>>>()
    val result: LiveData<Result<ResponseValueModel<UserModel>>> = _result

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun setLogInData(logInModel: LogInModel){
        if (
            logInModel.login.isEmpty() || logInModel.login.isBlank() ||
            logInModel.userPassword.isEmpty() || logInModel.userPassword.isBlank()
        ){
            setResult(result = ErrorResult(exception = Exception()), errorType = DataEntryError())
            setIsShown(isShown = true)
            return
        }
        viewModelScope.launch {
            setResult(result = PendingResult())
            val getUserUseCase = GetUserUseCase(
                profileRepository = profileRepositoryImpl,
                logInModel = logInModel
            )
            val result = getUserUseCase.execute()
            _result.value = result
        }
    }

    fun setIsShown(isShown: Boolean){
        _isShown.value = isShown

    }

    fun setResult(result: Result<ResponseValueModel<UserModel>>,errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("LoginViewModel setResult errorType = null")
        }
        _result.value = result
    }

    fun clearErrorType(){
        _errorType.value = OtherError()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","LoginViewModel onCleared")
    }
}
