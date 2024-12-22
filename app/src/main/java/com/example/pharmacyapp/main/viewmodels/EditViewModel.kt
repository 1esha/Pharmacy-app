package com.example.pharmacyapp.main.viewmodels

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
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserByIdUseCase
import com.example.pharmacyapp.R
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.launch


class EditViewModel: ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _resultGetUserById = MutableLiveData<Result<ResponseValueModel<UserModel>>>(PendingResult())
    val resultGetUserById: LiveData<Result<ResponseValueModel<UserModel>>> = _resultGetUserById

    private val _resultEditUser = MutableLiveData<Result<ResponseModel>>()
    val resultEditUser: LiveData<Result<ResponseModel>> = _resultEditUser

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _isShownSuccessResultGetUserById = MutableLiveData<Boolean>(false)
    val isShownSuccessResultGetUserById: LiveData<Boolean> = _isShownSuccessResultGetUserById

    private val _isShownSuccessResultEditUser = MutableLiveData<Boolean>(false)
    val isShownSuccessResultEditUser: LiveData<Boolean> = _isShownSuccessResultEditUser

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun getUserById(userId: Int){
        if (userId == UNAUTHORIZED_USER){
            setResult(result = ErrorResult(exception = Exception()), errorType = IdentificationError())
            return
        }
        val getUserByIdUseCase = GetUserByIdUseCase(
            profileRepository = profileRepositoryImpl,
            userId = userId
        )
        viewModelScope.launch {
            val result = getUserByIdUseCase.execute()
            _resultGetUserById.value = result
        }

    }

    fun editUser(userInfoModel: UserInfoModel, userId: Int){
        if (
            userId <= 0 ||
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
            val resultEditUser = profileRepositoryImpl.editUser(
                UserModel(
                    userId = userId,
                    userInfoModel = userInfoModel
                )
            )

            _resultEditUser.value = resultEditUser
        }
    }

    fun setResult(result: Result<ResponseValueModel<UserModel>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType ?: throw NullPointerException("EditViewModel setResult errorType = null")
        }
        _resultGetUserById.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    fun setIsShownSuccessResultEditUser(isShown: Boolean){
        _isShownSuccessResultEditUser.value = isShown
    }

    fun setIsShownSuccessResultGetUserById(isShown: Boolean){
        _isShownSuccessResultGetUserById.value = isShown
    }

    fun setIsShown(isShown: Boolean){
        _isShown.value = isShown
    }

}