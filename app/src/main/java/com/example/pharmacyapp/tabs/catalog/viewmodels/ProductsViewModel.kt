package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
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
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH = "KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH"
        const val KEY_IS_SHOWN_GET_ALL_FAVORITES = "KEY_IS_SHOWN_GET_ALL_FAVORITES"
    }

    val mediatorProduct = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetProductsByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductModel>?>>>>()

    private val resultAddFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultGetAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<FavoriteModel>>>>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val _listAllFavorites = MutableLiveData<List<*>>(listOf<FavoriteModel>())
    val listAllFavorites: LiveData<List<*>> = _listAllFavorites

    val isShownGetProductsByPath: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] ?: false

   val isShownGetAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] ?: false

    private val _listProducts = MutableLiveData<List<*>>()
    val listProducts: LiveData<List<*>> = _listProducts

    private val _listAllProducts = MutableLiveData<List<*>>()
    val listAllProducts: LiveData<List<*>> = _listAllProducts

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    init {
        mediatorProduct.addSource(resultGetProductsByPath) { r ->
            mediatorProduct.value = r
        }

        mediatorProduct.addSource(resultAddFavorite) { r ->
            mediatorProduct.value = r
        }

        mediatorProduct.addSource(resultGetAllFavorites) { r ->
            mediatorProduct.value = r
        }

        mediatorProduct.addSource(resultRemoveFavorite) { r ->
            mediatorProduct.value = r
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

    fun addFavorite(favoriteModel: FavoriteModel)  {
        val addFavoriteUseCase = AddFavoriteUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            favoriteModel = favoriteModel)

        viewModelScope.launch {

            val resultAddFavorite = addFavoriteUseCase.execute()
            this@ProductsViewModel.resultAddFavorite.value = MediatorResultsModel(
                type = TYPE_ADD_FAVORITE,
                result = resultAddFavorite
            )
        }

        Log.i("TAG","ProductsViewModel addFavorite")
    }

    fun getAllFavorites() {
        val getAllFavoritesUseCase = GetAllFavoritesUseCase(favoriteRepository = favoriteRepositoryImpl)
        viewModelScope.launch {
            val result = getAllFavoritesUseCase.execute()
            resultGetAllFavorites.value = MediatorResultsModel(
                type = TYPE_GET_ALL_FAVORITES,
                result = result
            )
        }

        Log.i("TAG","ProductsViewModel getAllFavorites")
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

    fun changeListAllFavorites(favoriteModel: FavoriteModel, isFavorite: Boolean) {
        val listAllFavorites = _listAllFavorites.value ?:
        throw NullPointerException("ProductsViewModel listAllFavorites = null")

        val currentListAllFavorites = mutableListOf<FavoriteModel>()

        listAllFavorites.forEach {
            val currentFavoriteModel = it as FavoriteModel
            currentListAllFavorites.add(currentFavoriteModel)
        }

        if (isFavorite) {
            currentListAllFavorites.add(favoriteModel)
        }
        else {
            currentListAllFavorites.remove(favoriteModel)
        }

        _listAllFavorites.value = currentListAllFavorites
    }

    fun setListAllFavorites(listAllFavorites: List<*>) {
        _listAllFavorites.value = listAllFavorites
    }

    fun setIsShownGetProductsByPath(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] = isShown
    }

    fun setIsShownGetAllFavorites(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] = isShown
    }

    fun setResultGetProductsByPath(result: Result<ResponseValueModel<List<ProductModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultGetProductsByPath.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCTS_BY_PATH,
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

    fun setResultGetAllFavorites(result: Result<ResponseValueModel<List<FavoriteModel>>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultGetAllFavorites.value = MediatorResultsModel(
            type = TYPE_GET_ALL_FAVORITES,
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

    fun setListProductsModel(listProductModel: List<*>) {
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