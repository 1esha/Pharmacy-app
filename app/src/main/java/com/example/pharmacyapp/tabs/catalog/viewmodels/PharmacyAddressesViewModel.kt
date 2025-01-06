package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.catalog.usecases.GetPharmacyAddressesUseCase
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.launch

class PharmacyAddressesViewModel : ViewModel() {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val _result =
        MutableLiveData<Result<ResponseValueModel<List<PharmacyAddressesModel>?>>>()
    val result: LiveData<Result<ResponseValueModel<List<PharmacyAddressesModel>?>>> = _result

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    private val _isInitPharmacyAddresses = MutableLiveData<Boolean>(true)

    private val _listSelectedPharmacyAddresses =
        MutableLiveData<MutableList<SelectedPharmacyAddressesModel>>(
            mutableListOf()
        )
    val listSelectedPharmacyAddresses: LiveData<MutableList<SelectedPharmacyAddressesModel>> =
        _listSelectedPharmacyAddresses

    private val _counterSelectedItems = MutableLiveData<Int>(0)
    val counterSelectedItems: LiveData<Int> = _counterSelectedItems

    fun getPharmacyAddresses() {
        val getPharmacyAddressesUseCase = GetPharmacyAddressesUseCase(
            catalogRepository = catalogRepositoryImpl
        )

        viewModelScope.launch {
            val result = getPharmacyAddressesUseCase.execute()
            _result.value = result
        }
    }

    fun setCounterSelectedItems(counter: Int?) {
        _counterSelectedItems.value = counter ?: -2
    }

    fun setInitPharmacyAddresses(list: List<*>) {
        val isInit = _isInitPharmacyAddresses.value ?: throw NullPointerException("PharmacyAddressesViewModel setInitPharmacyAddresses isInit = null")
        if (isInit) {
            list.forEach {
                _listSelectedPharmacyAddresses.value?.add(
                    SelectedPharmacyAddressesModel(
                        pharmacyAddressesModel = it as PharmacyAddressesModel,
                        isSelected = false
                    )
                )
            }
        }
        _isInitPharmacyAddresses.value = false
        Log.i("TAG","PharmacyAddressesViewModel setInitPharmacyAddresses _listSelectedPharmacyAddresses = ${_listSelectedPharmacyAddresses.value}")
    }

    fun setPharmacyAddresses(position: Int, isSelect: Boolean) {
        val item = _listSelectedPharmacyAddresses.value?.get(position)?: throw NullPointerException("PharmacyAddressesViewModel setPharmacyAddresses item = null")
        _listSelectedPharmacyAddresses.value?.removeAt(position)
        _listSelectedPharmacyAddresses.value?.add(position,SelectedPharmacyAddressesModel(
            pharmacyAddressesModel = item.pharmacyAddressesModel,
            isSelected = isSelect
        ))

        Log.i("TAG","PharmacyAddressesViewModel setPharmacyAddresses _listSelectedPharmacyAddresses = ${_listSelectedPharmacyAddresses.value}")
    }

    fun setIsShown(isShown: Boolean) {
        _isShown.value = isShown
    }

    fun setResult(
        result: Result<ResponseValueModel<List<PharmacyAddressesModel>?>>,
        errorType: ErrorType? = null
    ) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType
                ?: throw NullPointerException("PharmacyAddressesViewModel setResult errorType = null")
        }
        _result.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }
}