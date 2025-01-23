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
import com.example.domain.catalog.usecases.GetProductAvailabilityByPathUseCase
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import kotlinx.coroutines.launch

class FilterViewModel(
    private val catalogRepository: CatalogRepository<
            ResponseValueModel<List<ProductModel>?>,
            ResponseValueModel<List<ProductAvailabilityModel>?>,
            ResponseValueModel<List<PharmacyAddressesModel>?>,
            ResponseValueModel<FavoriteModel>,
            ResponseValueModel<List<FavoriteModel>>,
            ResponseModel>
): ViewModel() {

    val mediatorFilter = MediatorLiveData<MediatorResultsModel<*>>()

    val resultGetProductAvailabilityByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductAvailabilityModel>?>>>>()

    val resultGetProductsByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductModel>?>>>>()

    private val _listAllProducts = MutableLiveData<List<*>>()
    val listAllProducts: LiveData<List<*>> = _listAllProducts

    private val _listAllIdsProductsAvailability = MutableLiveData<List<*>>()
    val listAllIdsProductsAvailability: LiveData<List<*>> = _listAllIdsProductsAvailability

    private val _isShownGetProductsByPath = MutableLiveData<Boolean>(false)
    val isShownGetProductsByPath: LiveData<Boolean> = _isShownGetProductsByPath

    private val _isShownGetProductAvailabilityByPath = MutableLiveData<Boolean>(false)
    val isShownGetProductAvailabilityByPath: LiveData<Boolean> = _isShownGetProductAvailabilityByPath

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
            catalogRepository = catalogRepository,
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
            catalogRepository = catalogRepository,
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
        _isShownGetProductsByPath.value = isShown
    }

    fun setIsShownGetProductAvailabilityByPath(isShown: Boolean){
        _isShownGetProductAvailabilityByPath.value = isShown
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