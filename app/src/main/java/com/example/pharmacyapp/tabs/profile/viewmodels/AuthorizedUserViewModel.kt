package com.example.pharmacyapp.tabs.profile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
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
    private val favoriteRepository: FavoriteRepository<
        ResponseValueModel<FavoriteModel>,
        ResponseValueModel<List<FavoriteModel>>,
        ResponseModel>): ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    val mediatorAuthorizedUser = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetUserById = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<UserModel>>>>()

    private val resultDeleteAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val _isShown = MutableLiveData(false)
    val isShown: LiveData<Boolean> = _isShown

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
        val deleteAllFavoriteUseCase = DeleteAllFavoriteUseCase(favoriteRepository = favoriteRepository)

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

    fun setIsShown(isShown: Boolean){
        _isShown.value = isShown
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
}