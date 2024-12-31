package com.example.pharmacyapp.tabs.catalog.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.launch

class ProductsViewModel: ViewModel() {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val _result = MutableLiveData<Result<ResponseValueModel<List<ProductModel>?>>>()
    val result: LiveData<Result<ResponseValueModel<List<ProductModel>?>>> = _result

    private val _isShown = MutableLiveData<Boolean>(false)
    val isShown: LiveData<Boolean> = _isShown

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
            _errorType.value = errorType?: throw NullPointerException("LoginViewModel setResult errorType = null")
        }
        _result.value = result
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }
}