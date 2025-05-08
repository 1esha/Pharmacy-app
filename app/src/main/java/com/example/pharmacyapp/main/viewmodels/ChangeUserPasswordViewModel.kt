package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.InputDataException
import com.example.domain.InvalidDataException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.ServerException
import com.example.domain.models.RequestModel
import com.example.domain.profile.usecases.ChangeUserPasswordUseCase
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_CHANGE_USER_PASSWORD
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChangeUserPasswordViewModel(
    private val profileRepositoryImpl: ProfileRepositoryImpl,
    private val enterTheData: String,
    private val invalidPassword: String
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Success(data = null))
    val stateScreen: StateFlow<Result> = _stateScreen

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

    fun onChangeUserPassword(
        isNetworkStatus: Boolean,
        newPassword: String,
        currentPassword: String,
        repeatCurrentPassword: String
    ){
        isShownToast = true
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {

                if (userId == UNAUTHORIZED_USER){
                    _stateScreen.value = Result.Error(exception = IdentificationException())
                    return@checkNetworkStatus
                }

                if (
                    newPassword.isEmpty() || newPassword.isBlank() ||
                    currentPassword.isEmpty() || currentPassword.isBlank() ||
                    repeatCurrentPassword.isEmpty() || repeatCurrentPassword.isBlank()
                    ) {
                    _stateScreen.value = Result.Error(exception = InputDataException())
                    return@checkNetworkStatus
                }

                if (currentPassword != repeatCurrentPassword) {
                    _stateScreen.value = Result.Error(exception = InvalidDataException(invalidMessage = invalidPassword))
                    return@checkNetworkStatus
                }

                val changeUserPasswordUseCase = ChangeUserPasswordUseCase(
                    profileRepository = profileRepositoryImpl,
                    userId = userId,
                    oldUserPassword = currentPassword,
                    newUserPassword = newPassword
                )

                viewModelScope.launch {

                    onLoading()

                    delay(MIN_DELAY)

                    changeUserPasswordUseCase.execute().collect { result ->
                        if (result is Result.Error){
                            _stateScreen.value = result
                            return@collect
                        }

                        val requestModel = RequestModel(
                            type = TYPE_CHANGE_USER_PASSWORD,
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

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        onLoading()
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                _stateScreen.value = Result.Success(data = null)
            },
            disconnectionListener = ::onDisconnect
        )
    }

    fun onError(exception: Exception,toast: (String?) -> Unit,block: () -> Unit){
        try {
            if (exception is ServerException || exception is InputDataException || exception is InvalidDataException){
                val messageToast = when(exception){
                    is ServerException -> exception.serverMessage
                    is InputDataException -> enterTheData
                    is InvalidDataException -> exception.invalidMessage
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
}