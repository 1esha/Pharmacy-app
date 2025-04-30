package com.example.pharmacyapp.tabs.home.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.advertisements.AdvertisementRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.advertisements.usecases.GetHomeAdvertisementUseCase
import com.example.domain.advertisements.usecases.GetRecommendedProductsUseCase
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.RequestModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeViewModel(
    private val advertisementRepositoryImpl: AdvertisementRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isFillRecommendedProducts = true

    private var isInitFillCounterAdvertisements = true

    private val network = Network()

    private val _listProductModel = MutableStateFlow<List<ProductModel>>(emptyList())
    val listProductModel = _listProductModel.asStateFlow()

    private val _listAdvertisements = MutableStateFlow<List<String>?>(null)
    val listAdvertisements = _listAdvertisements.asStateFlow()

    private val _selectedPosition = MutableStateFlow<Int>(-1)
    val selectedPosition = _selectedPosition.asStateFlow()

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests) {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    val getHomeAdvertisementUseCase = GetHomeAdvertisementUseCase(
                        advertisementRepository = advertisementRepositoryImpl
                    )

                    val getRecommendedProductsUseCase = GetRecommendedProductsUseCase(
                        advertisementRepository = advertisementRepositoryImpl
                    )

                    viewModelScope.launch {
                        val resultGetHomeAdvertisement = getHomeAdvertisementUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_HOME_ADVERTISEMENT,
                                result = result
                            )
                        }

                        val resultGetRecommendedProducts = getRecommendedProductsUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_RECOMMENDED_PRODUCTS,
                                result = result
                            )
                        }


                        val combinedFlow = combine(
                            resultGetHomeAdvertisement,
                            resultGetRecommendedProducts
                        ) { getHomeAdvertisement, getRecommendedProducts ->

                            return@combine listOf(
                                getHomeAdvertisement,
                                getRecommendedProducts
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

    fun onDestroyView(){
        isFillRecommendedProducts = true
    }

    fun tryAgain(isNetworkStatus: Boolean){
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listProductModel: List<ProductModel>,
        listAdvertisements: List<String>,
    ){
        if (isShownFillData){
            _listProductModel.value = listProductModel
            _listAdvertisements.value = listAdvertisements
        }

        isShownFillData = false
    }

    fun fillRecommendedProducts(listProductModel: List<ProductModel>,block: (List<List<ProductModel>>) -> Unit){
        if (isFillRecommendedProducts) {
            val listChunkedProductModel = listProductModel.chunked(2)

            block(listChunkedProductModel)
        }
        if (listProductModel.isNotEmpty()) isFillRecommendedProducts = false
    }

    fun fillHorizontalLayoutRecommendedProducts(listProductModel: List<ProductModel>,block: (ProductModel,Boolean,Boolean,String,String,String) -> Unit){
        var isFirst = true
        listProductModel.forEach { productModel ->

            val originalPrice = productModel.price
            val discount = productModel.discount
            val sumDiscount = ((discount / 100) * originalPrice)
            val price = originalPrice - sumDiscount

            val textDiscount = "-${productModel.discount.roundToInt()}%"

            val isDiscount = productModel.discount > 0.0



            val textOriginalPrice = originalPrice.roundToInt().toString()
            val textPrice = price.roundToInt().toString()

            block(productModel,isDiscount,isFirst,textDiscount,textOriginalPrice,textPrice)

            isFirst = false
        }
    }

    fun setSelectedPosition(newPosition: Int){
        _selectedPosition.value = newPosition
    }

    fun initFillCounterAdvertisements(block: () -> Unit){
        if (isInitFillCounterAdvertisements) {
            block()
        }
        if (_listAdvertisements.value != null) isInitFillCounterAdvertisements = false
    }

    fun fillCounterAdvertisements(newPosition: Int, block: (Int, List<String>) -> Unit){
        try {
            if (_listAdvertisements.value != null){
                block(newPosition,_listAdvertisements.value!!)
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    companion object {
        const val TYPE_GET_HOME_ADVERTISEMENT = "TYPE_GET_HOME_ADVERTISEMENT"
        const val TYPE_GET_RECOMMENDED_PRODUCTS = "TYPE_GET_RECOMMENDED_PRODUCTS"
    }

}
