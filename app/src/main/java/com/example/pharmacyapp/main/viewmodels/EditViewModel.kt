package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.InputDataException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.ServerException
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.DeleteUserUseCase
import com.example.domain.profile.usecases.EditUserUseCase
import com.example.domain.profile.usecases.GetUserByIdUseCase
import com.example.pharmacyapp.TYPE_DELETE_USER
import com.example.pharmacyapp.TYPE_EDIT_USER
import com.example.pharmacyapp.TYPE_GET_USER_BY_ID
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class EditViewModel(
    private val profileRepositoryImpl: ProfileRepositoryImpl
) : ViewModel() {
    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _userModel = MutableStateFlow<UserModel?>(null)
    val userModel = _userModel.asStateFlow()

    private var newUserModel: UserModel? = null

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isShownInstallUI = true

    private var isShownToast = true

    private var isInit = true

    private val network = Network()

    private var userId = UNAUTHORIZED_USER

    fun initValues(
        userId: Int
    ){
        if (isInit){
            this.userId = userId

            isInit = false
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests){

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

    fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun onError(exception: Exception,enterTheData: String,toast: (String?) -> Unit,block: () -> Unit){
        try {
            if (exception is ServerException || exception is InputDataException){
                val messageToast = when(exception){
                    is ServerException -> exception.serverMessage
                    is InputDataException -> enterTheData
                    else -> throw IllegalArgumentException()
                }
                if (isShownToast) toast(messageToast)
                isShownToast = false
            }
            else{
                block()
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun tryAgain(isNetworkStatus: Boolean){
        _userModel.value = null

        isShownSendingRequests = true
        isShownFillData = true
        isShownInstallUI = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(userModel: UserModel){
        if (isShownFillData){
            _userModel.value = userModel
        }

        isShownFillData = false
    }

    fun installUI(block: () -> Unit){
        if (isShownInstallUI) {
            block()
        }
        isShownInstallUI = false
    }

    fun onSuccessfullyEdited(block: (UserModel) -> Unit){
        try {
            _userModel.value = newUserModel
            if (isShownToast) block(_userModel.value!!)
            isShownToast = false
        }
       catch (e: Exception){
           Log.e("TAG",e.stackTraceToString())
           _stateScreen.value = Result.Error(exception = e)
       }
    }

    fun onEditUser(
        isNetworkStatus: Boolean,
        userInfoModel: UserInfoModel
    ){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                isShownToast = true

                if (userInfoModel.isEmpty()) {
                    _stateScreen.value = Result.Error(exception = InputDataException())
                    return@checkNetworkStatus
                }

                newUserModel = UserModel(
                    userId = userId,
                    userInfoModel = userInfoModel
                )

                editUser(userInfoModel = userInfoModel)
            },
            disconnectionListener = ::onDisconnect
        )
    }

    private fun editUser(userInfoModel: UserInfoModel) {
        onLoading()

        val editUserUseCase = EditUserUseCase(
            profileRepository = profileRepositoryImpl,
            userModel = UserModel(
                userId = userId,
                userInfoModel = userInfoModel
            )
        )
        viewModelScope.launch {
            editUserUseCase.execute().collect { result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val requestModel = RequestModel(
                    type = TYPE_EDIT_USER,
                    result = result
                )

                _stateScreen.value = Result.Success(
                    data = listOf(requestModel)
                )
            }
        }
    }

    fun deleteUser(isNetworkStatus: Boolean,) {
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                onLoading()

                if (userId == UNAUTHORIZED_USER) {
                    _stateScreen.value = Result.Error(exception = IdentificationException())
                    return@checkNetworkStatus
                }

                val deleteUserUseCase = DeleteUserUseCase(
                    profileRepository = profileRepositoryImpl,
                    userId = userId
                )
                viewModelScope.launch {
                    deleteUserUseCase.execute().collect { result ->
                        if (result is Result.Error){
                            _stateScreen.value = result
                            return@collect
                        }

                        val requestModel = RequestModel(
                            type = TYPE_DELETE_USER,
                            result = result
                        )

                        _stateScreen.value = Result.Success(
                            data = listOf(requestModel)
                        )
                    }
                }
            },
            disconnectionListener = ::onDisconnect
            )
    }
}