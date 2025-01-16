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
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductAvailabilityByPathUseCase
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.launch

class FilterViewModel: ViewModel() {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val _resultGetProductAvailabilityByPath = MutableLiveData<Result<ResponseValueModel<List<ProductAvailabilityModel>?>>>()
    val resultGetProductAvailabilityByPath: LiveData<Result<ResponseValueModel<List<ProductAvailabilityModel>?>>> = _resultGetProductAvailabilityByPath

    private val _resultGetProductsByPath = MutableLiveData<Result<ResponseValueModel<List<ProductModel>?>>>()
    val resultGetProductsByPath: LiveData<Result<ResponseValueModel<List<ProductModel>?>>> = _resultGetProductsByPath

    private val _listAllProducts = MutableLiveData<List<*>>()
    val listAllProducts: LiveData<List<*>> = _listAllProducts

//    private val _arrayListIdsFilteredProducts = MutableLiveData<ArrayList<Int>>()
//    val arrayListIdsFilteredProducts: LiveData<ArrayList<Int>> = _arrayListIdsFilteredProducts

//    private val _arrayListIdsAddresses = MutableLiveData<ArrayList<Int>>()
//    val arrayListIdsAddresses: LiveData<ArrayList<Int>> = _arrayListIdsAddresses

    private val _listAllIdsProductsAvailability = MutableLiveData<List<*>>()
    val listAllIdsProductsAvailability: LiveData<List<*>> = _listAllIdsProductsAvailability

//    private val _defaultPriceFrom = MutableLiveData<Double>()
//    val defaultPriceFrom: LiveData<Double> = _defaultPriceFrom
//
//    private val _defaultPriceUpTo = MutableLiveData<Double>()
//    val defaultPriceUpTo: LiveData<Double> = _defaultPriceUpTo

    private val _isShownGetProductsByPath = MutableLiveData<Boolean>(false)
    val isShownGetProductsByPath: LiveData<Boolean> = _isShownGetProductsByPath

    private val _isShownGetProductAvailabilityByPath = MutableLiveData<Boolean>(false)
    val isShownGetProductAvailabilityByPath: LiveData<Boolean> = _isShownGetProductAvailabilityByPath

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun getProductAvailabilityByPath(path: String) {
        val getProductAvailabilityByPathUseCase = GetProductAvailabilityByPathUseCase(
            catalogRepository = catalogRepositoryImpl,
            path = path
        )

        viewModelScope.launch {
            val result = getProductAvailabilityByPathUseCase.execute()

            _resultGetProductAvailabilityByPath.value = result

            Log.i("TAG","FilterViewModel getProductAvailabilityByPath result = $result")
        }
    }

    fun getProductsByPath(path: String) {
        val getProductsByPathUseCase = GetProductsByPathUseCase(
            catalogRepository = catalogRepositoryImpl,
            path = path
        )
        viewModelScope.launch {
            val result = getProductsByPathUseCase.execute()
            _resultGetProductsByPath.value = result
        }
    }

//    fun setDefaultPriceFromUpTo(priceFrom: Double, priceUpTo: Double) {
//        _defaultPriceFrom.value = priceFrom
//        _defaultPriceUpTo.value = priceUpTo
//    }

    fun setIsShownGetProductsByPath(isShown: Boolean){
        _isShownGetProductsByPath.value = isShown
    }

    fun setIsShownGetProductAvailabilityByPath(isShown: Boolean){
        _isShownGetProductAvailabilityByPath.value = isShown
    }

    fun setResultGetProductAvailabilityByPath(result: Result<ResponseValueModel<List<ProductAvailabilityModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FilterViewModel setResult errorType = null")
        }
        _resultGetProductAvailabilityByPath.value = result
    }

    fun setResultGetProductsByPath(result: Result<ResponseValueModel<List<ProductModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FilterViewModel setResult errorType = null")
        }
        _resultGetProductsByPath.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    fun setListAllProducts(listProducts: List<*>) {
        _listAllProducts.value = listProducts
    }

    fun setListAllIdsProductsAvailability(list: List<*>) {
        _listAllIdsProductsAvailability.value = list
    }

//    fun setArrayListIdsAddresses(arrayListIdsAddresses: ArrayList<Int>) {
//        _arrayListIdsAddresses.value = arrayListIdsAddresses
//    }

//    fun setArrayListIdsFilteredProducts(arrayListIdsFilteredProducts: ArrayList<Int>) {
//        _arrayListIdsFilteredProducts.value = arrayListIdsFilteredProducts
//        Log.i("TAG","FilterViewModel arrayListIdsFilteredProducts.value = ${_arrayListIdsFilteredProducts.value}")
//    }
}