package com.example.pharmacyapp.tabs.profile.viewmodels

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.favorite.usecases.DeleteAllFavoriteUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserByIdUseCase
import com.example.pharmacyapp.TYPE_DELETE_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.launch

class AuthorizedUserViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val profileRepositoryImpl: ProfileRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_USER_BY_ID = "KEY_IS_SHOWN_GET_USER_BY_ID"
        const val KEY_IS_SHOWN_DELETE_ALL_FAVORITES = "KEY_IS_SHOWN_DELETE_ALL_FAVORITES"
    }

    val mediatorAuthorizedUser = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetUserById = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<UserModel>>>>()

    private val resultDeleteAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    val isShownGetUserById: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_USER_BY_ID] ?: false

    val isShownDeleteAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_DELETE_ALL_FAVORITES] ?: false

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    private val _userModelLiveData = MutableLiveData<UserModel>()
    val userModelLivedata: LiveData<UserModel> = _userModelLiveData

    init {
        mediatorAuthorizedUser.addSource(resultGetUserById) { result ->
            mediatorAuthorizedUser.value = result
        }

        mediatorAuthorizedUser.addSource(resultDeleteAllFavorites) { result ->
            mediatorAuthorizedUser.value = result
        }
    }

    fun getUserById(userId: Int){
        if (userId == UNAUTHORIZED_USER){

            resultGetUserById.value = MediatorResultsModel(
                type = TYPE_GET_USER_BY_ID,
                result = ErrorResult(exception = Exception())
            )
            return
        }
        viewModelScope.launch {
            val getUserByIdUseCase = GetUserByIdUseCase(
                profileRepository = profileRepositoryImpl,
                userId = userId
            )

            val result = getUserByIdUseCase.execute()

            resultGetUserById.value = MediatorResultsModel(
                type = TYPE_GET_USER_BY_ID,
                result = result
            )

        }
    }

    fun deleteAllFavorites() {
        val deleteAllFavoriteUseCase = DeleteAllFavoriteUseCase(favoriteRepository = favoriteRepositoryImpl)

        viewModelScope.launch {
            val result = deleteAllFavoriteUseCase.execute()

            resultDeleteAllFavorites.value = MediatorResultsModel(
                type = TYPE_DELETE_ALL_FAVORITES,
                result = result
            )
        }

    }

    fun setResultGetUserById(result: Result<ResponseValueModel<UserModel>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType ?: throw NullPointerException("AuthorizedUserViewModel setResult errorType = null")
        }
        resultGetUserById.value = MediatorResultsModel(
            type = TYPE_GET_USER_BY_ID,
            result = result
        )
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    fun setIsShownGetUserById(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_USER_BY_ID] = isShown
    }

    fun setIsShownDeleteAllFavorites(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_DELETE_ALL_FAVORITES] = isShown
    }

    fun updateUserModel(firstName: String, lastName: String, city: String) {
        val currentUserModel = _userModelLiveData.value

        if (currentUserModel != null) {
            val newUserModel = UserModel(
                userId = currentUserModel.userId,
                userInfoModel = UserInfoModel(
                    firstName = firstName,
                    lastName = lastName,
                    email = currentUserModel.userInfoModel.email,
                    phoneNumber = currentUserModel.userInfoModel.phoneNumber,
                    userPassword = currentUserModel.userInfoModel.userPassword,
                    city = city
                )
            )

            _userModelLiveData.value = newUserModel
        }

    }

    fun setUserModel(userModel: UserModel){
        _userModelLiveData.value = userModel
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","AuthorizedUserViewModel onCleared")
    }
}