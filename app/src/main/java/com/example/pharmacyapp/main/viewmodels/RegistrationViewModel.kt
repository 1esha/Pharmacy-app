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
import com.example.domain.IdentificationError
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import com.example.domain.profile.usecases.GetUserIdUseCase
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.launch

class RegistrationViewModel : ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _resultCreateUser = MutableLiveData<Result<ResponseModel>>()
    val resultCreateUser: LiveData<Result<ResponseModel>> = _resultCreateUser

    private val _userId = MutableLiveData(UNAUTHORIZED_USER)
    val userId: LiveData<Int> = _userId

    private val _isShown = MutableLiveData(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    private val _isSetupCity = MutableLiveData(true)
    val isSetupCity: LiveData<Boolean> = _isSetupCity

    fun createUser(userInfoModel: UserInfoModel) {
        if (
            userInfoModel.firstName.isEmpty() || userInfoModel.firstName.isBlank() ||
            userInfoModel.lastName.isEmpty() || userInfoModel.lastName.isBlank() ||
            userInfoModel.email.isEmpty() || userInfoModel.email.isBlank() ||
            userInfoModel.phoneNumber.isEmpty() || userInfoModel.phoneNumber.isBlank() ||
            userInfoModel.userPassword.isEmpty() || userInfoModel.userPassword.isBlank() ||
            userInfoModel.city.isEmpty() || userInfoModel.city.isBlank()
        ) {
            setResult(
                result = ErrorResult(exception = Exception()),
                errorType = DataEntryError()
            )
            return
        }
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

            when (resultGetUserId) {
                is PendingResult -> {}
                is SuccessResult -> {
                    _userId.value = resultGetUserId.value?.value ?: throw NullPointerException("RegistrationViewModel userId = null")
                    _resultCreateUser.value = resultCreateUser
                }
                is ErrorResult -> {
                    setResult(
                        result = ErrorResult(exception = resultGetUserId.exception),
                        errorType = IdentificationError()
                    )
                }
            }

        }

    }

    fun setIsShown(isShown: Boolean) {
        _isShown.value = isShown
    }

    fun setResult(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType ?: throw NullPointerException("RegistrationViewModel setResult errorType = null")
        }
        _resultCreateUser.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }


    override fun onCleared() {
        super.onCleared()
        Log.i("TAG", "RegistrationViewModel onCleared")
    }

}