package com.example.pharmacyapp.tabs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.IdentificationError
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserByIdUseCase
import kotlinx.coroutines.launch

class AuthorizedUserViewModel: ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<UserModel>>>(PendingResult())
    val result: LiveData<Result<ResponseValueModel<UserModel>>> = _result

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun getUserById(userId: Int){
        if (userId <= 0){
            setResult(result = ErrorResult(exception = Exception()), errorType = IdentificationError())
            return
        }
        viewModelScope.launch {
            val getUserByIdUseCase = GetUserByIdUseCase(
                profileRepository = profileRepositoryImpl,
                userId = userId
            )

            val result = getUserByIdUseCase.execute()

            _result.value = result

        }
    }

    fun setResult(result: Result<ResponseValueModel<UserModel>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType ?: throw NullPointerException("AuthorizedUserViewModel setResult errorType = null")
        }
        _result.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    fun setIsShown(isShown: Boolean){
        _isShown.value = isShown
    }
}