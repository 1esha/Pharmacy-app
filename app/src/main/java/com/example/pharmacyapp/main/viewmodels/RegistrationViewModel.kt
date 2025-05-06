package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.InputDataException
import com.example.domain.InvalidDataException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.ServerException
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import com.example.domain.profile.usecases.GetUserIdUseCase
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_CREATE_USER
import com.example.pharmacyapp.TYPE_GET_USER_ID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class RegistrationViewModel(
    private val profileRepositoryImpl: ProfileRepositoryImpl,
    private val enterTheData: String,
    private val wrongPhoneNumberDialed: String
) : ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Success(data = null))
    val stateScreen: StateFlow<Result> = _stateScreen

    private var isShownToast = true

    private var network = Network()

    val isSetupCityText = MutableStateFlow(true)

    private var numberPhone: String? = null

    private var userInfoModel: UserInfoModel? = null

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

                    if (userInfoModel.phoneNumber.length !in 11..12){
                        _stateScreen.value = Result.Error(exception = InvalidDataException(invalidMessage = wrongPhoneNumberDialed))
                        return@checkNetworkStatus
                    }

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

                            this@RegistrationViewModel.userInfoModel = userInfoModel

                            _stateScreen.value = Result.Success(
                                data = listOf(RequestModel(
                                    type = TYPE_CREATE_USER,
                                    result = resultCreateUser
                                ))
                            )
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

    fun getUserId(isNetworkStatus: Boolean){
        try {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    if (userInfoModel == null){
                        _stateScreen.value = Result.Error(exception = NullPointerException())
                        return@checkNetworkStatus
                    }

                    Log.i("TAG","userInfoModel = $userInfoModel")
                    val getUserIdUseCase = GetUserIdUseCase(
                        profileRepository = profileRepositoryImpl,
                        userInfoModel = userInfoModel!!
                    )



                    viewModelScope.launch {
                        delay(MIN_DELAY)

                        getUserIdUseCase.execute().collect { resultGetUserId ->
                            if (resultGetUserId is Result.Error){
                                _stateScreen.value = resultGetUserId
                                return@collect
                            }
                            numberPhone = userInfoModel!!.phoneNumber

                            _stateScreen.value = Result.Success(
                                data = listOf(RequestModel(
                                    type = TYPE_GET_USER_ID,
                                    result = resultGetUserId
                                ))
                            )
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

    fun saveNumberPhone(block: (String?) -> Unit){
        block(numberPhone)
    }

    fun setIsShownToast(){
        isShownToast = true
    }
}