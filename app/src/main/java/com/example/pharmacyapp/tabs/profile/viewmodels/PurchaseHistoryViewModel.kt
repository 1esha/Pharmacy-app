package com.example.pharmacyapp.tabs.profile.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.orders.OrdersRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductsByIdsUseCase
import com.example.domain.models.OrderProductModel
import com.example.domain.models.RequestModel
import com.example.domain.orders.models.OrderModel
import com.example.domain.orders.usecases.GetPurchaseHistoryUseCase
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_EMPTY_RESULT
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_IDS
import com.example.pharmacyapp.TYPE_GET_PURCHASE_HISTORY
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PurchaseHistoryViewModel(
    private val ordersRepositoryImpl: OrdersRepositoryImpl,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _listOrderProductModel = MutableStateFlow<List<OrderProductModel>>(emptyList())
    val listOrderProductModel = _listOrderProductModel.asStateFlow()

    private val network = Network()

    private var userId = UNAUTHORIZED_USER

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isInit = true

    fun initValues(userId: Int){
        if (isInit) this.userId = userId

        isInit = false
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests){

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    try {
                        onLoading()

                        if (userId == UNAUTHORIZED_USER){
                            _stateScreen.value = Result.Error(exception = IdentificationException())
                        }


                        val getPurchaseHistoryUseCase = GetPurchaseHistoryUseCase(
                            ordersRepository = ordersRepositoryImpl,
                            userId = userId
                        )


                        viewModelScope.launch {
                            getPurchaseHistoryUseCase.execute().collect { resultPurchaseHistory ->
                                if (resultPurchaseHistory is Result.Error){
                                    _stateScreen.value = Result.Error(exception = resultPurchaseHistory.exception)
                                    return@collect
                                }

                                val response = resultPurchaseHistory.asSuccess()
                                if (response == null){
                                    _stateScreen.value = Result.Error(exception = NullPointerException())
                                    return@collect
                                }

                                val data = response.data as ResponseValueModel<*>

                                val _listOrderModel = data.value as List<*>
                                val listOrderModel = _listOrderModel.map { it as OrderModel }

                                val emptyListRequest = listOf(
                                    RequestModel(
                                        type = TYPE_EMPTY_RESULT,
                                        result = Result.Success(data = null)
                                    )
                                )

                                if (listOrderModel.isEmpty()) {
                                    _stateScreen.value = Result.Success(data = emptyListRequest)
                                    return@collect
                                }

                                val listIdsProducts = listOrderModel.map { it.productId }

                                val getProductsByIdsUseCase = GetProductsByIdsUseCase(
                                    catalogRepository = catalogRepositoryImpl,
                                    listIdsProducts = listIdsProducts
                                )

                                getProductsByIdsUseCase.execute().collect { resultProductsByIds ->
                                    if (resultProductsByIds is Result.Error){
                                        _stateScreen.value = Result.Error(exception = resultProductsByIds.exception)
                                    }
                                    else {
                                        val listRequest = listOf(
                                            RequestModel(
                                                type = TYPE_GET_PRODUCTS_BY_IDS,
                                                result = resultProductsByIds
                                            ),
                                            RequestModel(
                                                type = TYPE_GET_PURCHASE_HISTORY,
                                                result = resultPurchaseHistory
                                            )
                                        )

                                        _stateScreen.value = Result.Success(data = listRequest)
                                    }
                                }
                            }
                        }
                    }
                    catch (e: Exception){
                        Log.e("TAG",e.stackTraceToString())
                        _stateScreen.value = Result.Error(exception = e)
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
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun installEmptyList(){
        _listOrderProductModel.value = emptyList()
    }

    fun fillData(
        listOrderModel: List<OrderModel>,
        listProductModel: List<ProductModel>
    ) {
        if (isShownFillData) {
            val mutableListOrderProductModel = mutableListOf<OrderProductModel>()

            listOrderModel.forEach { orderModel ->
                val productModel = listProductModel.find { it.productId == orderModel.productId }

                mutableListOrderProductModel.add(
                    OrderProductModel(
                        orderModel = orderModel,
                        productModel = productModel!!
                    )
                )
            }
            _listOrderProductModel.value = mutableListOrderProductModel
        }
        isShownFillData = false
    }
}