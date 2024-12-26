package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DataEntryError
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.IdentificationError
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.DeleteUserUseCase
import com.example.domain.profile.usecases.EditUserUseCase
import com.example.domain.profile.usecases.GetUserByIdUseCase
import com.example.pharmacyapp.TYPE_DELETE_USER
import com.example.pharmacyapp.TYPE_EDIT_USER
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.launch


class EditViewModel : ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    val mediatorLiveData = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetUserById = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<UserModel>>>>()

    private val resultEditUser = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultDeleteUser = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _isShownSuccessResultGetUserById = MutableLiveData<Boolean>(false)
    val isShownSuccessResultGetUserById: LiveData<Boolean> = _isShownSuccessResultGetUserById

    private val _isShownSuccessResultEditUser = MutableLiveData<Boolean>(false)
    val isShownSuccessResultEditUser: LiveData<Boolean> = _isShownSuccessResultEditUser

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    init {
        mediatorLiveData.addSource(resultGetUserById) { r ->
            mediatorLiveData.value = r
        }

        mediatorLiveData.addSource(resultEditUser) { r ->
            mediatorLiveData.value = r
        }

        mediatorLiveData.addSource(resultDeleteUser) { r ->
            mediatorLiveData.value = r
        }

    }

    fun getUserById(userId: Int) {
        if (userId == UNAUTHORIZED_USER) {
            _errorType.value = IdentificationError()
            resultGetUserById.value = MediatorResultsModel(
                type = TYPE_GET_USER_BY_ID,
                result = ErrorResult(exception = Exception())
            )

            return
        }
        val getUserByIdUseCase = GetUserByIdUseCase(
            profileRepository = profileRepositoryImpl,
            userId = userId
        )
        viewModelScope.launch {
            val result = getUserByIdUseCase.execute()
            resultGetUserById.value = MediatorResultsModel(
                type = TYPE_GET_USER_BY_ID,
                result = result
            )

        }

    }

    fun editUser(userInfoModel: UserInfoModel, userId: Int) {
        if (
            userId <= 0 ||
            userInfoModel.firstName.isEmpty() || userInfoModel.firstName.isBlank() ||
            userInfoModel.lastName.isEmpty() || userInfoModel.lastName.isBlank() ||
            userInfoModel.email.isEmpty() || userInfoModel.email.isBlank() ||
            userInfoModel.phoneNumber.isEmpty() || userInfoModel.phoneNumber.isBlank() ||
            userInfoModel.userPassword.isEmpty() || userInfoModel.userPassword.isBlank() ||
            userInfoModel.city.isEmpty() || userInfoModel.city.isBlank()
        ) {
            _errorType.value = DataEntryError()
            resultEditUser.value = MediatorResultsModel(
                type = TYPE_EDIT_USER,
                result = ErrorResult(exception = Exception())
            )

            return
        }
        viewModelScope.launch {
            val editUserUseCase = EditUserUseCase(
                profileRepository = profileRepositoryImpl,
                userModel = UserModel(
                    userId = userId,
                    userInfoModel = userInfoModel
                )
            )
            val result = editUserUseCase.execute()
            resultEditUser.value = MediatorResultsModel(
                type = TYPE_EDIT_USER,
                result = result
            )

        }
    }

    fun deleteUser(userId: Int) {
        if (userId == UNAUTHORIZED_USER) {
            _errorType.value = IdentificationError()
            resultDeleteUser.value = MediatorResultsModel(
                type = TYPE_DELETE_USER,
                result = ErrorResult(exception = Exception())
            )

            return
        }
        viewModelScope.launch {
            val deleteUserUseCase = DeleteUserUseCase(
                profileRepository = profileRepositoryImpl,
                userId = userId
            )

            val result = deleteUserUseCase.execute()
            resultDeleteUser.value = MediatorResultsModel(
                type = TYPE_DELETE_USER,
                result = result
            )

        }
    }

    fun setResultGetUserById(
        result: Result<ResponseValueModel<UserModel>>,
        errorType: ErrorType? = null
    ) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value =
                errorType ?: throw NullPointerException("EditViewModel setResult errorType = null")
        }
        resultGetUserById.value = MediatorResultsModel(
            type = TYPE_GET_USER_BY_ID,
            result = result
        )

    }

    fun setResultEditUser(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value =
                errorType ?: throw NullPointerException("EditViewModel setResult errorType = null")
        }
        resultEditUser.value = MediatorResultsModel(
            type = TYPE_EDIT_USER,
            result = result
        )

    }

    fun setResultDeleteUser(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value =
                errorType ?: throw NullPointerException("EditViewModel setResult errorType = null")
        }
        resultDeleteUser.value = MediatorResultsModel(
            type = TYPE_DELETE_USER,
            result = result
        )

    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    fun setIsShownSuccessResultEditUser(isShown: Boolean) {
        _isShownSuccessResultEditUser.value = isShown
    }

    fun setIsShownSuccessResultGetUserById(isShown: Boolean) {
        _isShownSuccessResultGetUserById.value = isShown
    }

    fun setIsShown(isShown: Boolean) {
        _isShown.value = isShown
    }

}