package com.example.pharmacyapp.tabs.basket.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.basket.models.BasketModel
import com.example.domain.basket.usecases.DeleteProductsFromBasketUseCase
import com.example.domain.basket.usecases.GetProductsFromBasketUseCase
import com.example.domain.basket.usecases.UpdateNumberProductsInBasketUseCase
import com.example.domain.models.RequestModel
import com.example.domain.models.SelectedBasketModel
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_DELETE_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.toArrayListInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BasketViewModel(
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _listSelectedBasketModel = MutableStateFlow<List<SelectedBasketModel>>(emptyList())
    val listSelectedBasketModel = _listSelectedBasketModel.asStateFlow()

    private val network = Network()

    private var isShownSendingRequests = true

    private var isInstallAdapter = true

    private var isInit = true

    private var isShownFillData = true

    private var isShownRemoveProducts = true

    private var currentSelectedBasketModel: SelectedBasketModel? = null

    private var userId = UNAUTHORIZED_USER

    fun initValues(
        userId: Int
    ){
        if (isInit){
            this.userId = userId
        }
        isInit = false
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests){

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    onLoading()

                    val getProductsFromBasketUseCase = GetProductsFromBasketUseCase(
                        basketRepository = basketRepositoryImpl,
                        userId = userId
                    )

                    viewModelScope.launch {
                        getProductsFromBasketUseCase.execute().collect { result ->
                            val requestModel = RequestModel(
                                type = TYPE_GET_PRODUCTS_FROM_BASKET,
                                result = result
                            )

                            if (result is Result.Error){
                                _stateScreen.value = result
                                return@collect
                            }

                            _stateScreen.value = Result.Success(
                                data = listOf(requestModel)
                            )
                        }
                    }
                },
                disconnectionListener = ::onDisconnect
            )
        }
        isShownSendingRequests = false
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        val mutableListSelectedBasketModel = _listSelectedBasketModel.value.toMutableList()
        mutableListSelectedBasketModel.clear()
        _listSelectedBasketModel.value = mutableListSelectedBasketModel

        isShownSendingRequests = true
        isShownFillData = true
        isInstallAdapter = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun onClickCheckBox(newSelectedBasketModel: SelectedBasketModel){
        currentSelectedBasketModel = newSelectedBasketModel
        changeListSelectedBasket()
    }

    fun onUpdateNumberProduct(isNetworkStatus: Boolean, newSelectedBasketModel: SelectedBasketModel){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                currentSelectedBasketModel = newSelectedBasketModel

                val productId = newSelectedBasketModel.basketModel.productModel.productId
                val newNumberProducts = newSelectedBasketModel.basketModel.numberProducts


                updateNumberProduct(
                    userId = userId,
                    productId = productId,
                    numberProducts = newNumberProducts
                )
            },
            disconnectionListener = ::onDisconnect
        )
    }

    fun onDeleteProductsFromBasket(isNetworkStatus: Boolean){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                isShownRemoveProducts = true

                val listIdsProducts = _listSelectedBasketModel.value.filter { it.isSelect }.map { it.basketModel.productModel.productId }

                deleteProductsFromBasket(listIdsProducts = listIdsProducts)
            },
            disconnectionListener = ::onDisconnect
        )
    }

    private fun deleteProductsFromBasket(listIdsProducts: List<Int>){
        onLoading()

        val deleteProductsFromBasketUseCase = DeleteProductsFromBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            listIdsProducts = listIdsProducts
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            deleteProductsFromBasketUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val listRequests = listOf(RequestModel(
                    type = TYPE_DELETE_PRODUCTS_FROM_BASKET,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = listRequests)
            }
        }

    }

    private fun updateNumberProduct(
        userId: Int ,
        productId: Int ,
        numberProducts: Int
    ){
        onLoading()

        val updateNumberProductsInBasketUseCase = UpdateNumberProductsInBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            updateNumberProductsInBasketUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val listRequests = listOf(RequestModel(
                    type = TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = listRequests)
            }
        }
    }

    fun changeListSelectedBasket(){
        try {
            if (currentSelectedBasketModel != null) {
                val productId = currentSelectedBasketModel!!.basketModel.productModel.productId

                val mutableListSelectedBasketModel = _listSelectedBasketModel.value.toMutableList()

                val oldSelectedBasketModel = mutableListSelectedBasketModel.find { it.basketModel.productModel.productId == productId }

                val index = mutableListSelectedBasketModel.indexOf(oldSelectedBasketModel)

                mutableListSelectedBasketModel.removeAt(index)
                mutableListSelectedBasketModel.add(index, currentSelectedBasketModel!!)

                _listSelectedBasketModel.value = emptyList()
                _listSelectedBasketModel.value = mutableListSelectedBasketModel
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun removeProductsByIds(){
        try {
            val mutableListSelectedBasketModel = _listSelectedBasketModel.value.toMutableList()
            if (isShownRemoveProducts) {
                val listIdsProducts = _listSelectedBasketModel.value.filter { it.isSelect }.map { it.basketModel.productModel.productId }
                listIdsProducts.forEach { productId ->
                    val selectedBasketModel = mutableListSelectedBasketModel.find { it.basketModel.productModel.productId == productId }
                    mutableListSelectedBasketModel.remove(selectedBasketModel)
                }
            }
            isShownRemoveProducts = false

            isInstallAdapter = true

            _listSelectedBasketModel.value = emptyList()
            _listSelectedBasketModel.value = mutableListSelectedBasketModel
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }


    fun fillData(list: List<BasketModel>){
        if (isShownFillData){
            val mutableListSelectedBasketModel = mutableListOf<SelectedBasketModel>()

            list.forEach {
                mutableListSelectedBasketModel.add(SelectedBasketModel(basketModel = it))
            }

            _listSelectedBasketModel.value = mutableListSelectedBasketModel
        }

        isShownFillData = false
    }

    fun onSelectAll(isSelect: Boolean){
        val listSelectedBasketModel = _listSelectedBasketModel.value.map { it.copy(isSelect = isSelect) }

        _listSelectedBasketModel.value = listSelectedBasketModel
    }

    fun installUI(mutableListSelectedBasketModel: MutableList<SelectedBasketModel>,block: (Boolean,Boolean,Boolean) -> Unit){
        val isEmpty = mutableListSelectedBasketModel.isEmpty()
        val isAllSelect = mutableListSelectedBasketModel.all { it.isSelect }
        val isLeastOneSelected = mutableListSelectedBasketModel.any { it.isSelect }
        block(isEmpty,isAllSelect,isLeastOneSelected)

    }

    fun installAdapter(block: () -> Unit){
        val size = _listSelectedBasketModel.value.size
        if (isInstallAdapter) block()
        if (size != 0) isInstallAdapter = false
    }

    fun navigateToOrderMaking(navigate:(ArrayList<Int>,ArrayList<Int>) -> Unit){
        val arrayListIdsSelectedBasketModel = _listSelectedBasketModel.value.filter { it.isSelect }
            .map { it.basketModel.productModel.productId }.toArrayListInt()
        val arrayListNumberProductsSelectedBasketModels = _listSelectedBasketModel.value.filter { it.isSelect }
            .map { it.basketModel.numberProducts }.toArrayListInt()
        navigate(arrayListIdsSelectedBasketModel,arrayListNumberProductsSelectedBasketModels)
    }

    fun setIsInstallAdapter(isInstallAdapter: Boolean){
        this.isInstallAdapter = isInstallAdapter
    }
}