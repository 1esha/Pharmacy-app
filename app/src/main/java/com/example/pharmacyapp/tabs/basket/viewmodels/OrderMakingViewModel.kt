package com.example.pharmacyapp.tabs.basket.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.orders.OrdersRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.basket.models.BasketModel
import com.example.domain.basket.usecases.DeleteProductsFromBasketUseCase
import com.example.domain.basket.usecases.GetProductsFromBasketUseCase
import com.example.domain.basket.usecases.UpdateNumbersProductsInBasketUseCase
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.usecases.GetProductAvailabilityByAddressIdUseCase
import com.example.domain.catalog.usecases.UpdateNumbersProductsInPharmacyUseCase
import com.example.domain.models.NumberProductsModel
import com.example.domain.models.RequestModel
import com.example.domain.orders.usecases.CreateOrderUseCase
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.TYPE_CREATE_ORDER
import com.example.pharmacyapp.TYPE_DELETE_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID
import com.example.pharmacyapp.TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET
import com.example.pharmacyapp.TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class OrderMakingViewModel(
    private val basketRepositoryImpl: BasketRepositoryImpl,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val ordersRepositoryImpl: OrdersRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _listBasketModel = MutableStateFlow<List<BasketModel>>(emptyList())
    val listBasketModel = _listBasketModel.asStateFlow()

    private val network = Network()

    private var isShownSendingRequests = true

    private var isInit = true

    private var isShownFillData = true

    private var isShownFillingLayoutProductsForOrder = true

    private val _listAvailableQuantity = MutableStateFlow<List<Int>>(emptyList())
    val listAvailableQuantity = _listAvailableQuantity.asStateFlow()

    private var listProductAvailabilityModel = listOf<ProductAvailabilityModel>()

    private val mutableListNumberProductsModel = mutableListOf<NumberProductsModel>()

    private var userId = UNAUTHORIZED_USER

    private var addressId: Int? = null

    private val _address = MutableStateFlow(EMPTY_STRING)
    val address = _address.asStateFlow()

    private val _city = MutableStateFlow(EMPTY_STRING)
    val city = _city.asStateFlow()

    fun initValues(
        userId: Int?,
        addressId: Int?,
        city: String?,
        address: String?,
        arrayListIdsProducts: ArrayList<Int>?,
        arrayListNumberProducts: ArrayList<Int>?,
    ){
        if (isInit){
            this.userId = userId?:UNAUTHORIZED_USER

            this.addressId = addressId

            _city.value = city ?: EMPTY_STRING

            _address.value = address ?: EMPTY_STRING

            if (arrayListIdsProducts != null && arrayListNumberProducts != null){
                for (i in 0..< arrayListIdsProducts.size){
                    val productId = arrayListIdsProducts[i]
                    val numberProducts = arrayListNumberProducts[i]
                    mutableListNumberProductsModel.add(NumberProductsModel(
                        productId = productId,
                        numberProducts = numberProducts
                    ))
                }
            }
        }
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

                        val listIdsProducts = mutableListNumberProductsModel.map { it.productId }

                        val getProductsFromBasketUseCase = GetProductsFromBasketUseCase(
                            basketRepository = basketRepositoryImpl,
                            userId = userId
                        )

                        val getProductAvailabilityByAddressIdUseCase = GetProductAvailabilityByAddressIdUseCase(
                            catalogRepository = catalogRepositoryImpl,
                            addressId = addressId!!,
                            listIdsProducts = listIdsProducts
                        )

                        viewModelScope.launch {
                            val resultGetProductsFromBasket = getProductsFromBasketUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_PRODUCTS_FROM_BASKET,
                                    result = result
                                )
                            }
                            val resultGetProductAvailabilityByAddressId = getProductAvailabilityByAddressIdUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID,
                                    result = result
                                )
                            }

                            val combineFlow = combine(
                                resultGetProductsFromBasket,
                                resultGetProductAvailabilityByAddressId
                            ) { getProductsFromBasket, getProductAvailabilityByAddressId ->
                                return@combine listOf(
                                    getProductsFromBasket,
                                    getProductAvailabilityByAddressId
                                )
                            }

                            combineFlow.collect { listResults ->
                                listResults.forEach { requestModel ->
                                    if (requestModel.result is Result.Error){
                                        _stateScreen.value = requestModel.result
                                        return@collect
                                    }
                                }

                                _stateScreen.value = Result.Success(
                                    data = listResults
                                )
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

    fun onDestroyView(){
        isShownFillingLayoutProductsForOrder = true
    }

    fun tryAgain(isNetworkStatus: Boolean){
        isShownSendingRequests = true
        isShownFillData = true

        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listBasketMode: List<BasketModel>,
        listProductAvailabilityModel: List<ProductAvailabilityModel>
    ){
        try {
            if (isShownFillData){
                this.listProductAvailabilityModel = listProductAvailabilityModel

                val mutableNumberProductsModel = mutableListOf<NumberProductsModel>()

                listProductAvailabilityModel.forEach { productAvailabilityModel ->
                    Log.i("TAG","CHECK addressId = ${productAvailabilityModel.addressId}")
                    mutableListNumberProductsModel.forEach { numberProductsModel ->
                        if (numberProductsModel.productId == productAvailabilityModel.productId){

                            Log.d("TAG","addressId = ${productAvailabilityModel.addressId}")
                            Log.d("TAG","productId = ${numberProductsModel.productId}")
                            Log.d("TAG","productAvailabilityModel.numberProducts = ${productAvailabilityModel.numberProducts}\nnumberProductsModel.numberProducts = ${numberProductsModel.numberProducts}")

                            if (productAvailabilityModel.numberProducts < numberProductsModel.numberProducts){
                                mutableNumberProductsModel.add(numberProductsModel.copy(numberProducts = productAvailabilityModel.numberProducts))
                            }
                            else mutableNumberProductsModel.add(numberProductsModel.copy(numberProducts = numberProductsModel.numberProducts))
                        }
                    }
                }

                _listAvailableQuantity.value = mutableNumberProductsModel.map { it.numberProducts }

                _listBasketModel.value = listBasketMode.filter { basketModel ->
                    mutableNumberProductsModel.any { it.productId == basketModel.productModel.productId && it.numberProducts > 0 }
                }.map { basketModel ->
                    val numberProducts = mutableNumberProductsModel.find { it.productId == basketModel.productModel.productId }!!.numberProducts
                    return@map basketModel.copy(numberProducts = numberProducts)
                }
            }
            isShownFillData = false
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun fillingLayoutProductsForOrder(listBasketModel: List<BasketModel>,block: (BasketModel,Boolean) -> Unit){
        if (isShownFillingLayoutProductsForOrder && listBasketModel.isNotEmpty()) {

            var counter = 1
            listBasketModel.forEach { basketModel ->

                val isVisibleDivider = counter != listBasketModel.size

                block(basketModel,isVisibleDivider)

                counter++
            }

            isShownFillingLayoutProductsForOrder = false
        }
    }

    fun fillingTotalAmount(block: (String,String,String,String,String,Boolean) -> Unit){

        var numberPiecesTotal = 0
        var orderAmount  = 0.0
        var discountTotal = 0.0
        var clubDiscountTotal = 0.0
        var totalPrice = 0.0

        _listBasketModel.value.forEach { basketModel ->
            val sumDiscount = (basketModel.productModel.discount / 100) * basketModel.productModel.price
            val priceDiscounted = basketModel.productModel.price - sumDiscount
            val sumClubDiscount = (CLUB_DISCOUNT / 100) * priceDiscounted
            val priceClubDiscounted = priceDiscounted - sumClubDiscount

            numberPiecesTotal += basketModel.numberProducts

            orderAmount += (basketModel.productModel.price * basketModel.numberProducts).roundToInt()

            discountTotal += (sumDiscount * basketModel.numberProducts).roundToInt()
            clubDiscountTotal += (sumClubDiscount * basketModel.numberProducts).roundToInt()
            totalPrice += (priceClubDiscounted * basketModel.numberProducts).roundToInt()
        }
        val textNumberPiecesTotal = numberPiecesTotal.toString()
        val textOrderAmount = orderAmount.roundToInt().toString()
        val textDiscountTotal = "-${discountTotal.roundToInt()}"
        val textClubDiscountTotal = "-${clubDiscountTotal.roundToInt()}"
        val textTotalPrice = totalPrice.roundToInt().toString()

        val isVisibleDiscount = discountTotal > 0

        block(textNumberPiecesTotal,textOrderAmount,textDiscountTotal,textClubDiscountTotal,textTotalPrice,isVisibleDiscount)
    }

    fun installLayoutAvailability(listAvailableQuantity: List<Int>,block: (Int,Int,Boolean) -> Unit){

        val availableQuantity = listAvailableQuantity.sum()
        val totalNumber = mutableListNumberProductsModel.sumOf { it.numberProducts }

        val isVisible = availableQuantity != totalNumber

        block(availableQuantity,totalNumber,isVisible)
    }

    fun onClickPlaceAnOrder(isNetworkStatus: Boolean){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                val mutableListIdsProductsForDeleteInBasket = mutableListOf<Int>()

                val mutableListNumberProductsForUpdateInBasket = mutableListOf<NumberProductsModel>()

                listProductAvailabilityModel.forEach { productAvailabilityModel ->

                    mutableListNumberProductsModel.forEach { numberProductsModel ->

                        if (numberProductsModel.productId == productAvailabilityModel.productId) {

                            val currentNumberProduct = numberProductsModel.numberProducts - productAvailabilityModel.numberProducts

                            if (currentNumberProduct < 0) {
                                mutableListIdsProductsForDeleteInBasket.add(numberProductsModel.productId)
                            }
                            else {
                                mutableListNumberProductsForUpdateInBasket.add(
                                    numberProductsModel.copy(numberProducts = currentNumberProduct)
                                )
                            }

                        }

                    }
                }

                val listNumberProductsCurrent = _listBasketModel.value.map { basketModel ->
                    NumberProductsModel(
                        productId = basketModel.productModel.productId,
                        numberProducts = basketModel.numberProducts
                    )
                }

                makeOrder(
                    listIdsProductsForDeleteInBasket = mutableListIdsProductsForDeleteInBasket,
                    listNumberProductsForUpdateInBasket = mutableListNumberProductsForUpdateInBasket,
                    listNumberProductsCurrent = listNumberProductsCurrent
                )
            },
            disconnectionListener = ::onDisconnect
        )
    }

    private fun makeOrder(
        listIdsProductsForDeleteInBasket: List<Int>,
        listNumberProductsForUpdateInBasket: List<NumberProductsModel>,
        listNumberProductsCurrent: List<NumberProductsModel>,
    ){
        try {
            val deleteProductsFromBasketUseCase = DeleteProductsFromBasketUseCase(
                basketRepository = basketRepositoryImpl,
                userId = userId,
                listIdsProducts = listIdsProductsForDeleteInBasket
            )

            val updateNumbersProductsInBasketUseCase = UpdateNumbersProductsInBasketUseCase(
                basketRepository = basketRepositoryImpl,
                userId = userId,
                listNumberProductsModel = listNumberProductsForUpdateInBasket
            )

            val updateNumbersProductsInPharmacyUseCase = UpdateNumbersProductsInPharmacyUseCase(
                catalogRepository = catalogRepositoryImpl,
                addressId = addressId!!,
                listNumberProductsModel = listNumberProductsCurrent
            )

            val createOrderUseCase = CreateOrderUseCase(
                ordersRepository = ordersRepositoryImpl,
                userId = userId,
                addressId = addressId!!,
                listNumberProductsModel = listNumberProductsCurrent
            )
            viewModelScope.launch {
                onLoading()

                val resultDeleteProductsFromBasket = deleteProductsFromBasketUseCase.execute().map { result->
                    return@map RequestModel(
                        type = TYPE_DELETE_PRODUCTS_FROM_BASKET,
                        result = result
                    )
                }

                val resultUpdateNumbersProductsInBasket = updateNumbersProductsInBasketUseCase.execute().map { result ->
                    return@map RequestModel(
                        type = TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET,
                        result = result
                    )
                }

                val resultUpdateNumbersProductsInPharmacy = updateNumbersProductsInPharmacyUseCase.execute().map { result ->
                    return@map RequestModel(
                        type = TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY,
                        result = result
                    )
                }

                val resultCreateOrder = createOrderUseCase.execute().map { result->
                    return@map RequestModel(
                        type = TYPE_CREATE_ORDER,
                        result = result
                    )
                }

                val combineFlow = when {
                    listIdsProductsForDeleteInBasket.isNotEmpty() && listNumberProductsForUpdateInBasket.isNotEmpty() -> {
                        combine(
                            resultDeleteProductsFromBasket,
                            resultUpdateNumbersProductsInBasket,
                            resultUpdateNumbersProductsInPharmacy,
                            resultCreateOrder
                        ) { deleteProductsFromBasket,updateNumbersProductsInBasket,updateNumbersProductsInPharmacy,createOrder ->
                            // И удаляем и обновляем
                            listOf(
                                deleteProductsFromBasket,
                                updateNumbersProductsInBasket,
                                updateNumbersProductsInPharmacy,
                                createOrder
                            )
                        }
                    }
                    listIdsProductsForDeleteInBasket.isNotEmpty() -> {
                        combine(
                            resultDeleteProductsFromBasket,
                            resultUpdateNumbersProductsInPharmacy,
                            resultCreateOrder
                        ) { deleteProductsFromBasket,updateNumbersProductsInPharmacy,createOrder ->
                            // Только удаляем
                            listOf(
                                deleteProductsFromBasket,
                                updateNumbersProductsInPharmacy,
                                createOrder
                            )
                        }
                    }
                    listNumberProductsForUpdateInBasket.isNotEmpty() -> {
                        combine(
                            resultUpdateNumbersProductsInBasket,
                            resultUpdateNumbersProductsInPharmacy,
                            resultCreateOrder
                        ) { updateNumbersProductsInBasket,updateNumbersProductsInPharmacy,createOrder ->
                            // Только обновляем
                            listOf(
                                updateNumbersProductsInBasket,
                                updateNumbersProductsInPharmacy,
                                createOrder
                            )
                        }
                    }
                    else -> throw IllegalArgumentException()
                }

                combineFlow.collect { listResults ->
                    listResults.forEach { requestModel ->
                        if (requestModel.result is Result.Error){
                            _stateScreen.value = requestModel.result
                            return@collect
                        }
                    }

                    _stateScreen.value = Result.Success(
                        data = listResults
                    )
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }
}