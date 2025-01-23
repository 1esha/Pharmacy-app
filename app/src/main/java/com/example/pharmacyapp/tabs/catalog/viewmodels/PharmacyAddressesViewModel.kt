package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.FavoriteModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetPharmacyAddressesUseCase
import com.example.domain.catalog.usecases.GetProductAvailabilityByPathUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import kotlinx.coroutines.launch

class PharmacyAddressesViewModel(
    private val catalogRepository: CatalogRepository<
        ResponseValueModel<List<ProductModel>?>,
        ResponseValueModel<List<ProductAvailabilityModel>?>,
        ResponseValueModel<List<PharmacyAddressesModel>?>,
        ResponseValueModel<FavoriteModel>,
        ResponseValueModel<List<FavoriteModel>>,
        ResponseModel>
) : ViewModel() {

    val mediatorPharmacyAddresses = MediatorLiveData<MediatorResultsModel<*>>()

    val resultGetPharmacyAddresses = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<PharmacyAddressesModel>?>>>>()

    val resultGetProductAvailabilityByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductAvailabilityModel>?>>>>()

    private val _isShownGetPharmacyAddresses = MutableLiveData<Boolean>(false)
    val isShownGetPharmacyAddresses: LiveData<Boolean> = _isShownGetPharmacyAddresses

    private val _isShownGetProductAvailabilityByPath = MutableLiveData<Boolean>(false)
    val isShownGetProductAvailabilityByPath: LiveData<Boolean> = _isShownGetProductAvailabilityByPath

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    private val _isInitPharmacyAddresses = MutableLiveData<Boolean>(true)

    private val _mutableListSelectedPharmacyAddresses = MutableLiveData<MutableList<SelectedPharmacyAddressesModel>>(mutableListOf())
    val mutableListSelectedPharmacyAddresses: LiveData<MutableList<SelectedPharmacyAddressesModel>> = _mutableListSelectedPharmacyAddresses

    private val _listPharmacyAddresses = MutableLiveData<List<*>>()
    val listPharmacyAddresses: LiveData<List<*>> = _listPharmacyAddresses

    private val _counterSelectedItems = MutableLiveData<Int>(0)
    val counterSelectedItems: LiveData<Int> = _counterSelectedItems

    init {

        mediatorPharmacyAddresses.addSource(resultGetPharmacyAddresses) { r ->
            mediatorPharmacyAddresses.value = r
        }

        mediatorPharmacyAddresses.addSource(resultGetProductAvailabilityByPath) { r ->
            mediatorPharmacyAddresses.value = r
        }

    }

    fun getPharmacyAddresses() {
        val getPharmacyAddressesUseCase = GetPharmacyAddressesUseCase(
            catalogRepository = catalogRepository
        )

        viewModelScope.launch {
            val result = getPharmacyAddressesUseCase.execute()
            resultGetPharmacyAddresses.value = MediatorResultsModel(
                type = TYPE_GET_PHARMACY_ADDRESSES,
                result = result
            )
        }
    }

    fun getProductAvailabilityByPath(path: String) {
        val getProductAvailabilityByPathUseCase = GetProductAvailabilityByPathUseCase(
            catalogRepository = catalogRepository,
            path = path
        )

        viewModelScope.launch {
            val result = getProductAvailabilityByPathUseCase.execute()

            resultGetProductAvailabilityByPath.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
                result = result
            )
        }
    }

    fun setCounterSelectedItems(counter: Int?) {
        _counterSelectedItems.value = counter ?: -1
    }

    fun setListPharmacyAddresses(list: List<*>) {
        _listPharmacyAddresses.value = list
    }

    fun setMutableListSelectedPharmacyAddresses(mutableList: MutableList<SelectedPharmacyAddressesModel>) {
        _mutableListSelectedPharmacyAddresses.value = mutableList
    }

    fun setInitPharmacyAddresses(list: List<*>, counter: Int) {
        val isInit = _isInitPharmacyAddresses.value ?: throw NullPointerException("PharmacyAddressesViewModel setInitPharmacyAddresses isInit = null")
        if (isInit) {
            list.forEach {
                val selectedPharmacyAddressesModel = it as SelectedPharmacyAddressesModel
                _mutableListSelectedPharmacyAddresses.value?.add(
                    SelectedPharmacyAddressesModel(
                        pharmacyAddressesModel = selectedPharmacyAddressesModel.pharmacyAddressesModel,
                        isSelected = selectedPharmacyAddressesModel.isSelected,
                        productAvailabilityModel = selectedPharmacyAddressesModel.productAvailabilityModel
                    )
                )
            }
            _counterSelectedItems.value = counter
            Log.i("TAG","PharmacyAddressesViewModel setInitPharmacyAddresses _listSelectedPharmacyAddresses = ${_mutableListSelectedPharmacyAddresses.value}")
        }
        _isInitPharmacyAddresses.value = false

    }

    fun setPharmacyAddresses(position: Int, isSelect: Boolean) {
        val item = _mutableListSelectedPharmacyAddresses.value?.get(position)?: throw NullPointerException("PharmacyAddressesViewModel setPharmacyAddresses item = null")
        _mutableListSelectedPharmacyAddresses.value?.removeAt(position)
        _mutableListSelectedPharmacyAddresses.value?.add(position,SelectedPharmacyAddressesModel(
            pharmacyAddressesModel = item.pharmacyAddressesModel,
            isSelected = isSelect,
            productAvailabilityModel = item.productAvailabilityModel
        ))

        Log.i("TAG","PharmacyAddressesViewModel setPharmacyAddresses _mutableListSelectedPharmacyAddresses = ${_mutableListSelectedPharmacyAddresses.value}")
    }

    fun setResultGetProductAvailabilityByPath(result: Result<ResponseValueModel<List<ProductAvailabilityModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("PharmacyAddressesViewModel setResult errorType = null")
        }
        resultGetProductAvailabilityByPath.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
            result = result
        )
    }

    fun setResultGetPharmacyAddresses(
        result: Result<ResponseValueModel<List<PharmacyAddressesModel>?>>,
        errorType: ErrorType? = null
    ) {
        if (result is ErrorResult && errorType != null) {
            _errorType.value = errorType
                ?: throw NullPointerException("PharmacyAddressesViewModel setResult errorType = null")
        }
        resultGetPharmacyAddresses.value = MediatorResultsModel(
            type = TYPE_GET_PHARMACY_ADDRESSES,
            result = result
        )
    }

    fun setIsShownGetPharmacyAddresses(isShown: Boolean) {
        _isShownGetPharmacyAddresses.value = isShown
    }

    fun setIsShownGetProductAvailabilityByPath(isShown: Boolean) {
        _isShownGetProductAvailabilityByPath.value = isShown
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }
}