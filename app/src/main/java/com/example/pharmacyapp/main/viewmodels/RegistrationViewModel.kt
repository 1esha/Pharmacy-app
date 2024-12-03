package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.usecases.CreateUserUseCase
import kotlinx.coroutines.launch

class RegistrationViewModel: ViewModel() {

    private val profileRepository = ProfileRepositoryImpl<Any>()

    private val _result = MutableLiveData<Result<ResponseModel>>(PendingResult())
    val result: LiveData<Result<ResponseModel>> = _result

    fun setUserInfo(userInfoModel: UserInfoModel){
        viewModelScope.launch {
            val createUserUseCase = CreateUserUseCase(
                profileRepository = profileRepository,
                userInfoModel = userInfoModel
            )
            val result = createUserUseCase.execute()
            _result.value = result
        }
    }
}