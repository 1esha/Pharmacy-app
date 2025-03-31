package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.InputDataException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.ServerException
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import com.example.domain.profile.usecases.GetUserIdUseCase
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_GET_USER_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RegistrationViewModel(
    private val profileRepositoryImpl: ProfileRepositoryImpl
) : ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Success(data = null))
    val stateScreen: StateFlow<Result> = _stateScreen

    private var isShownToast = true

    private var network = Network()

    val isSetupCityText = MutableStateFlow(true)

    fun register(isNetworkStatus: Boolean,userInfoModel: UserInfoModel){
        try {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    if (userInfoModel.isEmpty()){
                        _stateScreen.value = Result.Error(exception = InputDataException())
                        return@checkNetworkStatus
                    }

                    val getUserIdUseCase = GetUserIdUseCase(
                        profileRepository = profileRepositoryImpl,
                        userInfoModel = userInfoModel
                    )

                    val createUserUseCase = CreateUserUseCase(
                        profileRepository = profileRepositoryImpl,
                        userInfoModel = userInfoModel
                    )

                    viewModelScope.launch {
                        delay(MIN_DELAY)

                        createUserUseCase.execute().collect { resultCreateUser ->

                            if (resultCreateUser is Result.Error){
                                _stateScreen.value = resultCreateUser
                                return@collect
                            }

                            getUserIdUseCase.execute().collect { resultGetUserId ->
                                if (resultGetUserId is Result.Error){
                                    _stateScreen.value = resultGetUserId
                                }
                                else{
                                    _stateScreen.value = Result.Success(
                                        data = listOf(RequestModel(
                                            type = TYPE_GET_USER_ID,
                                            result = resultGetUserId
                                        ))
                                    )
                                }
                            }
                        }
                    }
                },
                disconnectionListener = ::onDisconnect
            )
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
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

    fun tryAgain(isNetworkStatus: Boolean, block: () -> Unit){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                _stateScreen.value = Result.Success(data = null)
                block()
            },
            disconnectionListener = ::onDisconnect
        )
    }

    fun setIsShownToast(){
        isShownToast = true
    }
}