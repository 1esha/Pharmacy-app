package com.example.pharmacyapp.tabs.profile.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.favorite.usecases.DeleteAllFavoriteUseCase
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserByIdUseCase
import com.example.pharmacyapp.TYPE_DELETE_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthorizedUserViewModel(
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val profileRepositoryImpl: ProfileRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel = _userModel.asStateFlow()

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isInit = true

    private val network = Network()

    private var userId = UNAUTHORIZED_USER

    fun initValues(userId: Int){
        if (isInit) {
            this.userId = userId

            isInit = false
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests) {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    if (userId == UNAUTHORIZED_USER) {
                        _stateScreen.value = Result.Error(exception = IdentificationException())
                        return@checkNetworkStatus
                    }

                    val getUserByIdUseCase = GetUserByIdUseCase(
                        profileRepository = profileRepositoryImpl,
                        userId = userId
                    )
                    viewModelScope.launch {
                        getUserByIdUseCase.execute().collect { result ->
                            val requestModel = RequestModel(
                                type = TYPE_GET_USER_BY_ID,
                                result = result
                            )

                            if (result is Result.Error){
                                _stateScreen.value = result
                                return@collect
                            }

                            _stateScreen.value = Result.Success(
                                data = listOf(requestModel)
                            )
                        }
                    }
                },
                disconnectionListener = ::onDisconnect
            )
        }
        isShownSendingRequests = false
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(userModel: UserModel){
        if (isShownFillData) _userModel.value = userModel
        isShownFillData = false
    }

    fun deleteAllFavorites() {
        onLoading()

        val deleteAllFavoriteUseCase = DeleteAllFavoriteUseCase(favoriteRepository = favoriteRepositoryImpl)

        viewModelScope.launch {
            deleteAllFavoriteUseCase.execute().collect { result ->
                val requestModel = RequestModel(
                    type = TYPE_DELETE_ALL_FAVORITES,
                    result = result
                )

                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                _stateScreen.value = Result.Success(
                    data = listOf(requestModel)
                )
            }
        }
    }

    fun listenResultFromEditUser(firstName: String?, lastName: String?, city: String?){
        try {

            val oldUserInfoModel = _userModel.value!!.userInfoModel
            val newUserModel = UserModel(
                userId = userId,
                userInfoModel = oldUserInfoModel.copy(
                    firstName = firstName!!,
                    lastName = lastName!!,
                    city = city!!
                )
            )

            _userModel.value = null
            _userModel.value = newUserModel
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }
}