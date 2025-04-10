package com.example.pharmacyapp.tabs.card.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CardViewModel: ViewModel() {

    private val _isAuthorizedUser = MutableStateFlow<Boolean?>(null)
    val isAuthorizedUser = _isAuthorizedUser.asStateFlow()


    fun installQRCode(
        userId: Int,
        numberPhone: String?,
        block: (String) -> Unit
    ){
        Log.i("TAG","nnn = $numberPhone")
        try {
            if (userId == UNAUTHORIZED_USER){
                _isAuthorizedUser.value = false
            }
            else {
                if (numberPhone == null) throw NullPointerException()
                _isAuthorizedUser.value = true
                block(numberPhone)
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())

            _isAuthorizedUser.value = null
        }
    }
}