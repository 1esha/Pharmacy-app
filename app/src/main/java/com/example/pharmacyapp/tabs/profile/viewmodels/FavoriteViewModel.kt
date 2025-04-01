package com.example.pharmacyapp.tabs.profile.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.basket.usecases.AddProductInBasketUseCase
import com.example.domain.basket.usecases.GetIdsProductsFromBasketUseCase
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.models.FavouriteBasketModel
import com.example.domain.models.RequestModel
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Класс [FavoriteViewModel] является viewModel для класса FavoriteFragment.
 */
class FavoriteViewModel(
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
) : ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private var network = Network()

    private var userId = UNAUTHORIZED_USER

    private val _listFavouriteBasketModel =
        MutableStateFlow<List<FavouriteBasketModel>>(emptyList())
    val listFavouriteBasketModel = _listFavouriteBasketModel.asStateFlow()

    private var currentProductId: Int? = null

    private var isShownSendingRequests = true

    private var isShownRemoveFavorite = true

    private var isShownFillData = true

    private var isInit = true

    private var isInstallAdapter = true


    fun initValues(
        userId: Int
    ) {
        if (isInit) {
            this.userId = userId

            isInit = false
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean) {
        if (isShownSendingRequests) {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    val getAllFavoritesUseCase = GetAllFavoritesUseCase(
                        favoriteRepository = favoriteRepositoryImpl
                    )
                    val getIdsProductsFromBasketUseCase = GetIdsProductsFromBasketUseCase(
                        basketRepository = basketRepositoryImpl,
                        userId = userId
                    )
                    viewModelScope.launch {

                        val resultGetAllFavorites = getAllFavoritesUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_ALL_FAVORITES,
                                result = result
                            )
                        }

                        val resultGetIdsProductsFromBasket =
                            getIdsProductsFromBasketUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
                                    result = result
                                )
                            }

                        val combinedFlow = combine(
                            resultGetAllFavorites,
                            resultGetIdsProductsFromBasket
                        ) { allFavorites, idsProductsFromBasket ->

                            return@combine listOf(
                                allFavorites,
                                idsProductsFromBasket
                            )
                        }

                        combinedFlow.collect { listResults ->
                            listResults.forEach { requestModel ->
                                if (requestModel.result is Result.Error) {
                                    _stateScreen.value = requestModel.result
                                    return@collect
                                }
                            }

                            _stateScreen.value = Result.Success(
                                data = listResults
                            )
                        }
                    }

                },
                disconnectionListener = ::onDisconnect
            )
        }
        isShownSendingRequests = false
    }

    private fun onLoading() {
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect() {
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean) {
        val mutableListFavouriteBasketModel = _listFavouriteBasketModel.value.toMutableList()
        mutableListFavouriteBasketModel.clear()
        _listFavouriteBasketModel.value = mutableListFavouriteBasketModel

        isShownSendingRequests = true
        isShownFillData = true
        isInstallAdapter = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listAllFavorite: List<FavoriteModel>,
        listIdsProductsFromBasket: List<Int>
    ) {
        if (isShownFillData) {
            val mutableListFavouriteBasketModel = mutableListOf<FavouriteBasketModel>()

            listAllFavorite.forEach { favoriteModel ->
                val isInBasket = listIdsProductsFromBasket.contains(favoriteModel.productId)
                mutableListFavouriteBasketModel.add(
                    FavouriteBasketModel(
                        favoriteModel = favoriteModel,
                        isInBasket = isInBasket
                    )
                )
            }

            _listFavouriteBasketModel.value = mutableListFavouriteBasketModel
        }

        isShownFillData = false
    }


    /**
     * Удаление  товара из "Избранного".
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет удален из "Избранного".
     */
    private fun removeFavorite(productId: Int) {
        val removeFavoritesUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )

        onLoading()

        viewModelScope.launch {
            delay(MIN_DELAY)

            removeFavoritesUseCase.execute().collect { result ->
                if (result is Result.Error) {
                    _stateScreen.value = result
                    return@collect
                }

                val data = listOf(
                    RequestModel(
                        type = TYPE_REMOVE_FAVORITES,
                        result = result
                    )
                )
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }


    /**
     * Добавление товара в корзину.
     *
     * Параметры:
     * [productId] - идентификатор товара;
     * [numberProducts] - количество товара, который будет добавлен. По умолчанию количесто = 1.
     */
    private fun addProductInBasket(productId: Int, numberProducts: Int = 1) {
        val addProductInBasketUseCase = AddProductInBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            numberProducts = numberProducts,
            productId = productId
        )

        onLoading()

        viewModelScope.launch {
            delay(MIN_DELAY)

            addProductInBasketUseCase.execute().collect { result ->
                if (result is Result.Error) {
                    _stateScreen.value = result
                    return@collect
                }

                val data = listOf(
                    RequestModel(
                        type = TYPE_ADD_PRODUCT_IN_BASKET,
                        result = result
                    )
                )
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }

    fun installUI(
        listFavouriteBasketModel: List<FavouriteBasketModel>,
        block: (Boolean) -> Unit
    ) {
        val isEmpty = listFavouriteBasketModel.isEmpty()
        block(isEmpty)
    }

    fun installAdapter(block: () -> Unit) {
        val size = _listFavouriteBasketModel.value.size
        if (isInstallAdapter) block()
        if (size != 0) isInstallAdapter = false
    }

    fun onClickDeleteFromFavorites(deletedProductId: Int) {
        isShownRemoveFavorite = true
        currentProductId = deletedProductId
        removeFavorite(productId = deletedProductId)
    }

    fun onClickAddInBasketFromFavorites(productId: Int) {
        currentProductId = productId
        addProductInBasket(productId = productId)
    }

    fun removeFromFavorites(){
        try {
            if (isShownRemoveFavorite) {
                val mutableListFavouriteBasketModel = _listFavouriteBasketModel.value.toMutableList()
                val favouriteBasketModel = mutableListFavouriteBasketModel.find { it.favoriteModel.productId == currentProductId }
                val index = mutableListFavouriteBasketModel.indexOf(favouriteBasketModel)

                mutableListFavouriteBasketModel.removeAt(index)

                _listFavouriteBasketModel.value = mutableListFavouriteBasketModel
            }
            isShownRemoveFavorite = false
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun changeListFavouriteBasketModel(){
        try {
            val mutableListFavouriteBasketModel = _listFavouriteBasketModel.value.toMutableList()
            val oldFavouriteBasketModel = mutableListFavouriteBasketModel.find { it.favoriteModel.productId == currentProductId }
            val index = mutableListFavouriteBasketModel.indexOf(oldFavouriteBasketModel)

            mutableListFavouriteBasketModel.removeAt(index)
            mutableListFavouriteBasketModel.add(index,oldFavouriteBasketModel!!.copy(isInBasket = true))

            _listFavouriteBasketModel.value = mutableListFavouriteBasketModel
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun setIsInstallAdapter(isInstallAdapter: Boolean){
        this.isInstallAdapter = isInstallAdapter
    }
}