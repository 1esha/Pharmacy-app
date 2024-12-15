package com.example.pharmacyapp.tabs.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserModel
import com.example.domain.profile.usecases.GetUserByIdUseCase
import kotlinx.coroutines.launch

class AuthorizedUserViewModel: ViewModel() {

    private val profileRepositoryImpl = ProfileRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<UserModel>>>(PendingResult())
    val result: LiveData<Result<ResponseValueModel<UserModel>>> = _result

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun getUserById(userId: Int, getStringById:(Int) -> String){
        if (userId <= 0){
            _message.value = getStringById(userId)
            return
        }
        viewModelScope.launch {
            val getUserByIdUseCase = GetUserByIdUseCase(
                profileRepository = profileRepositoryImpl,
                userId = userId
            )

            val result = getUserByIdUseCase.execute()

            _result.value = result

        }
    }
}