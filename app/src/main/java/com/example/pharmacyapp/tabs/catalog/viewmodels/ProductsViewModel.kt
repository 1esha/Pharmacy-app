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
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.launch

class ProductsViewModel: ViewModel() {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<List<ProductModel>?>>>(PendingResult())
    val result: LiveData<Result<ResponseValueModel<List<ProductModel>?>>> = _result

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

    private val _listProducts = MutableLiveData<List<*>>()
    val listProducts: LiveData<List<*>> = _listProducts

    private val _listAllProducts = MutableLiveData<List<*>>()
    val listAllProducts: LiveData<List<*>> = _listAllProducts

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun getProductsByPath(path: String) {
        val getProductsByPathUseCase = GetProductsByPathUseCase(
            catalogRepository = catalogRepositoryImpl,
            path = path
        )
        viewModelScope.launch {
            val result = getProductsByPathUseCase.execute()
            _result.value = result
        }
    }

    fun setIsShown(isShown: Boolean){
        _isShown.value = isShown
    }

    fun setResult(result: Result<ResponseValueModel<List<ProductModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        _result.value = result
    }

    fun setProductsModel(listProductModel: List<*>) {
        Log.i("TAG","ProductsViewModel setProductsModel listProductModel size = ${listProductModel.size}")
        _listProducts.value = listProductModel
    }

    fun setListAllProductsModel(listProductModel: List<*>) {
        Log.i("TAG","ProductsViewModel setListAllProductsModel listProductModel size = ${listProductModel.size}")
        _listAllProducts.value = listProductModel
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

    override fun onCleared() {
        super.onCleared()
        Log.i("TAG","ProductsViewModel onCleared")
    }
}