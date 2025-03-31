package com.example.pharmacyapp.tabs.catalog.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductAvailabilityByPathUseCase
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.models.RequestModel
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.getPrice
import com.example.pharmacyapp.toArrayListInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class FilterViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val network = Network()

    private var isShownSendingRequests = true

    private var isInit = true

    private var listProductAvailability = listOf<ProductAvailabilityModel>()

    private var listAllProducts = listOf<ProductModel>()

    private val _arrayListIdsSelectedAddresses = MutableStateFlow<ArrayList<Int>>(arrayListOf())
    val arrayListIdsSelectedAddresses = _arrayListIdsSelectedAddresses.asStateFlow()

    private val _isChecked = MutableStateFlow<Boolean>(false)
    val isChecked = _isChecked.asStateFlow()

    private var defaultPriceUpTo: Int = 0
    private var defaultPriceFrom: Int = 0

    private val _priceFrom = MutableStateFlow<Int>(defaultPriceFrom)
    val priceFrom get() =  _priceFrom.asStateFlow()

    private val _priceUpTo = MutableStateFlow<Int>(defaultPriceUpTo)
    val priceUpTo = _priceUpTo.asStateFlow()

    private var path = EMPTY_STRING

    fun initValues(
        isChecked: Boolean?,
        arrayListIdsSelectedAddresses: ArrayList<Int>?,
        priceFrom: Int?,
        priceUpTo: Int?,
        defaultPriceUpTo: Int?,
        defaultPriceFrom: Int?,
        path: String?
    ){
        try {
            if (isInit) {
                this.defaultPriceFrom = defaultPriceFrom?: throw NullPointerException()
                this.defaultPriceUpTo = defaultPriceUpTo?: throw NullPointerException()

                _isChecked.value = isChecked?: throw NullPointerException()
                _arrayListIdsSelectedAddresses.value = arrayListIdsSelectedAddresses?: throw NullPointerException()
                _priceFrom.value = priceFrom?: throw NullPointerException()
                _priceUpTo.value = priceUpTo?: throw NullPointerException()
                this.path = path?: throw NullPointerException()

                isInit = false
            }
        }
        catch (e: Exception){
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests) {

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    if (path == EMPTY_STRING) {
                        _stateScreen.value = Result.Error(exception = IllegalArgumentException())
                        return@checkNetworkStatus
                    }

                    val getProductAvailabilityByPathUseCase = GetProductAvailabilityByPathUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        path = path
                    )
                    val getProductsByPathUseCase = GetProductsByPathUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        path = path
                    )
                    viewModelScope.launch {
                        val resultGetProductAvailabilityByPath = getProductAvailabilityByPathUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
                                result = result
                            )
                        }

                        val resultGetProductsByPath = getProductsByPathUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCTS_BY_PATH,
                                result = result
                            )
                        }


                        val combinedFlow = combine(
                            resultGetProductAvailabilityByPath,
                            resultGetProductsByPath) { productAvailabilityByPath, productsByPath ->

                            return@combine listOf(
                                productAvailabilityByPath,
                                productsByPath
                            )
                        }

                        combinedFlow.collect{ listResults ->
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
                },
                disconnectionListener = ::onDisconnect
            )
        }
        isShownSendingRequests = false
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        isShownSendingRequests = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listProductAvailability: List<ProductAvailabilityModel>,
        listAllProducts: List<ProductModel>
    ){
        this.listProductAvailability = listProductAvailability
        this.listAllProducts = listAllProducts
    }

    fun listenResultFromPharmacyAddresses(
        newArrayListIdsSelectedAddresses: ArrayList<Int>?
    ){
        setArrayListIdsSelectedAddresses(newArrayListIdsSelectedAddresses = newArrayListIdsSelectedAddresses)
    }

    fun clearFilters(){
        setChecked(isChecked = false)
        setArrayListIdsSelectedAddresses(newArrayListIdsSelectedAddresses = arrayListOf())
        setPriceFrom(priceFrom = defaultPriceFrom)
        setPriceUpTo(priceUpTo = defaultPriceUpTo)
    }

    fun navigateToPharmacyAddresses(navigate: (String,ArrayList<Int>) -> Unit){
        navigate(path, _arrayListIdsSelectedAddresses.value)
    }

    fun backToProducts(back: (Boolean,Int,Int,ArrayList<Int>,ArrayList<Int>) -> Unit) {
        val arrayListIdsFilteredProducts = getFilteredArrayList(
            isChecked = _isChecked.value,
            priceFrom = _priceFrom.value,
            priceUpTo = _priceUpTo.value,
            arrayListSelectedIdsAddresses = _arrayListIdsSelectedAddresses.value
        )

        back(_isChecked.value, _priceFrom.value, _priceUpTo.value, _arrayListIdsSelectedAddresses.value, arrayListIdsFilteredProducts)
    }


    private fun setArrayListIdsSelectedAddresses( newArrayListIdsSelectedAddresses: ArrayList<Int>?){
        val oldArrayListIdsSelectedAddresses = _arrayListIdsSelectedAddresses.value

        _arrayListIdsSelectedAddresses.value = newArrayListIdsSelectedAddresses?:oldArrayListIdsSelectedAddresses
    }

    fun setChecked(isChecked: Boolean) {
        _isChecked.value = isChecked
    }

    fun setPriceFrom(priceFrom:Int){
        _priceFrom.value = priceFrom
    }

    fun setPriceUpTo(priceUpTo: Int){
        _priceUpTo.value = priceUpTo
    }

    fun checkCorrectPriceFrom(textPriceFrom: String){
        val currentPriceFrom = if (textPriceFrom.isEmpty() || textPriceFrom.isBlank()) {
            defaultPriceFrom
        } else {
            textPriceFrom.toDouble().roundToInt()
        }.toPriceFrom()

        _priceFrom.value = currentPriceFrom
    }

    fun checkCorrectPriceUpTo(textPriceUpTo: String,priceFrom: Int){
        val currentPriceUpTo = if (textPriceUpTo.isEmpty() || textPriceUpTo.isBlank()) {
            defaultPriceUpTo
        } else {
            textPriceUpTo.toDouble().roundToInt()
        }.toPriceUpTo(priceFrom = priceFrom)

        _priceUpTo.value = currentPriceUpTo
    }

    private fun Int.toPriceFrom(): Int{
        return if (this < defaultPriceFrom || this > defaultPriceUpTo) {
            defaultPriceFrom
        }
        else { this }
    }

    private fun Int.toPriceUpTo(priceFrom:Int): Int{
        return if (
            this > defaultPriceUpTo ||
            this < defaultPriceFrom ||
            priceFrom > this
        ) {
            defaultPriceUpTo
        }
        else { this }
    }

    private fun getFilteredArrayList(
        isChecked: Boolean,
        priceFrom: Int,
        priceUpTo: Int,
        arrayListSelectedIdsAddresses: ArrayList<Int>
    ): ArrayList<Int>{

        val listFilteredProductAvailability = listProductAvailability.filter { it.numberProducts > 0 }

        val listIdsProductsInSelectedPharmacy =
            if (arrayListSelectedIdsAddresses.isNotEmpty()) {
                listFilteredProductAvailability.filter { productAvailabilityModel ->
                    arrayListSelectedIdsAddresses.contains(productAvailabilityModel.addressId)
                }.map { it.productId }.distinct()
            }
            else {
                listFilteredProductAvailability.map { it.productId }.distinct()
            }

        val listProducts = listAllProducts.filter { productModel ->
            val price = getPrice(
                discount = productModel.discount,
                price = productModel.price
            )
            listIdsProductsInSelectedPharmacy.contains(productModel.productId) &&
            price in priceFrom..priceUpTo
        }

        val listFilteredProducts = if (isChecked){ listProducts.filter { it.discount > 0 } } else listProducts

        val arrayListFilteredProducts = listFilteredProducts.map { it.productId }.toArrayListInt()

        return arrayListFilteredProducts
    }
}