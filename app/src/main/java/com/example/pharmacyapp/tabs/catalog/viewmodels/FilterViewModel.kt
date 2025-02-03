package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
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
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import kotlinx.coroutines.launch

class FilterViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val catalogRepositoryImpl: CatalogRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH = "KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH"
        const val KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PATH = "KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PATH"
    }

    val mediatorFilter = MediatorLiveData<MediatorResultsModel<*>>()

    val resultGetProductAvailabilityByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductAvailabilityModel>?>>>>()

    val resultGetProductsByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductModel>?>>>>()

    private val _listAllProducts = MutableLiveData<List<*>>()
    val listAllProducts: LiveData<List<*>> = _listAllProducts

    private val _listAllIdsProductsAvailability = MutableLiveData<List<*>>()
    val listAllIdsProductsAvailability: LiveData<List<*>> = _listAllIdsProductsAvailability

    val isShownGetProductsByPath: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] ?: false

    val isShownGetProductAvailabilityByPath: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PATH] ?: false

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    init {

        mediatorFilter.addSource(resultGetProductsByPath) { r ->
            mediatorFilter.value = r
        }

        mediatorFilter.addSource(resultGetProductAvailabilityByPath) { r ->
            mediatorFilter.value = r
        }

    }

    fun getProductAvailabilityByPath(path: String) {
        val getProductAvailabilityByPathUseCase = GetProductAvailabilityByPathUseCase(
            catalogRepository = catalogRepositoryImpl,
            path = path
        )

        viewModelScope.launch {
            val result = getProductAvailabilityByPathUseCase.execute()

            resultGetProductAvailabilityByPath.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
                result = result
            )

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
            resultGetProductsByPath.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCTS_BY_PATH,
                result = result
            )
        }
    }

    fun setIsShownGetProductsByPath(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] = isShown
    }

    fun setIsShownGetProductAvailabilityByPath(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PATH] = isShown
    }

    fun setResultGetProductAvailabilityByPath(result: Result<ResponseValueModel<List<ProductAvailabilityModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FilterViewModel setResult errorType = null")
        }

        resultGetProductAvailabilityByPath.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
            result = result
        )
    }

    fun setResultGetProductsByPath(result: Result<ResponseValueModel<List<ProductModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FilterViewModel setResult errorType = null")
        }
        resultGetProductsByPath.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCTS_BY_PATH,
            result = result
        )
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

}