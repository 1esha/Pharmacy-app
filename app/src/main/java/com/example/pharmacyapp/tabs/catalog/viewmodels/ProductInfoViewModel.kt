package com.example.pharmacyapp.tabs.catalog.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductAvailabilityByProductIdUseCase
import com.example.domain.catalog.usecases.GetProductByIdUseCase
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID
import com.example.pharmacyapp.TYPE_GET_PRODUCT_BY_ID
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.launch

typealias ResultListProductAvailabilityModel = Result<ResponseValueModel<List<ProductAvailabilityModel>?>>

typealias ResultProductModel = Result<ResponseValueModel<ProductModel?>>

class ProductInfoViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_PRODUCT_BY_ID = "KEY_IS_SHOWN_GET_PRODUCT_BY_ID"
        const val KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID = "KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID"
    }

    val mediatorProductInfo = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetProductById = MutableLiveData<MediatorResultsModel<ResultProductModel>>()

    private val resultGetProductAvailabilityByProductId = MutableLiveData<MediatorResultsModel<ResultListProductAvailabilityModel>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultAddFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val _productModel = MutableLiveData<ProductModel>()
    val productModel: LiveData<ProductModel> = _productModel

    private val _listProductAvailability = MutableLiveData<List<ProductAvailabilityModel>?>()
    val listProductAvailability: LiveData<List<ProductAvailabilityModel>?> = _listProductAvailability

    val isShownGetProductById: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_BY_ID] ?: false

    val isShownGetProductAvailabilityByProductId: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID] ?: false

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    init {
        mediatorProductInfo.addSource(resultGetProductById) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultGetProductAvailabilityByProductId) { result ->
            mediatorProductInfo.value = result
        }
    }

    fun getProductById(productId: Int) {
        val getProductByIdUseCase = GetProductByIdUseCase(
            catalogRepository = catalogRepositoryImpl,
            productId = productId)

        viewModelScope.launch {
            val result = getProductByIdUseCase.execute()

            resultGetProductById.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_BY_ID,
                result = result
            )
        }
    }

    fun getProductAvailabilityByProductId(productId: Int) {
        val getProductAvailabilityByProductIdUseCase = GetProductAvailabilityByProductIdUseCase(
            catalogRepository = catalogRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            val result = getProductAvailabilityByProductIdUseCase.execute()

            resultGetProductAvailabilityByProductId.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID,
                result = result
            )
        }
    }

    fun addFavorite(favoriteModel: FavoriteModel)  {
        val addFavoriteUseCase = AddFavoriteUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            favoriteModel = favoriteModel)

        viewModelScope.launch {

            val resultAddFavorite = addFavoriteUseCase.execute()
            this@ProductInfoViewModel.resultAddFavorite.value = MediatorResultsModel(
                type = TYPE_ADD_FAVORITE,
                result = resultAddFavorite
            )
        }
    }

    fun removeFavorite(productId: Int) {
        val deleteByIdUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            val result = deleteByIdUseCase.execute()
            resultRemoveFavorite.value = MediatorResultsModel(
                type = TYPE_REMOVE_FAVORITES,
                result = result
            )
        }

    }

    fun setProductModel(productModel: ProductModel) {
        _productModel.value = productModel
    }

    fun setListProductAvailability(listProductAvailability: List<ProductAvailabilityModel>) {
        _listProductAvailability.value = listProductAvailability
    }

    fun setResultGetProductById(result: ResultProductModel, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductInfoViewModel setResult errorType = null")
        }
        resultGetProductById.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_BY_ID,
            result = result
        )
    }

    fun setResultGetProductAvailabilityByProductId(result: ResultListProductAvailabilityModel, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductInfoViewModel setResult errorType = null")
        }
        resultGetProductAvailabilityByProductId.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID,
            result = result
        )
    }

    fun setResultRemoveFavorites(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultRemoveFavorite.value = MediatorResultsModel(
            type = TYPE_REMOVE_FAVORITES,
            result = result
        )
    }

    fun setResultAddFavorite(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultAddFavorite.value = MediatorResultsModel(
            type = TYPE_ADD_FAVORITE,
            result = result
        )
    }

    fun setIsShownGetProductById(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_BY_ID] = isShown
    }

    fun setIsShownGetProductAvailabilityByProductId(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID] = isShown
    }


    fun clearErrorType() {
        _errorType.value = OtherError()
    }

}