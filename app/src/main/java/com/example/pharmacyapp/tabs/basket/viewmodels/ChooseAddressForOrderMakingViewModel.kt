package com.example.pharmacyapp.tabs.basket.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.usecases.GetPharmacyAddressesUseCase
import com.example.domain.catalog.usecases.GetProductAvailabilityByIdsProductsUseCase
import com.example.domain.models.AvailabilityProductsForOrderMakingModel
import com.example.domain.models.NumberProductsModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.usecases.GetCityByUserIdUseCase
import com.example.pharmacyapp.FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING
import com.example.pharmacyapp.TYPE_GET_CITY_BY_USER_ID
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChooseAddressForOrderMakingViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val profileRepositoryImpl: ProfileRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen.asStateFlow()

    private val _listAvailabilityProductsForOrderMakingModel = MutableStateFlow<List<AvailabilityProductsForOrderMakingModel>>(emptyList())
    val listAvailabilityProductsForOrderMakingModel = _listAvailabilityProductsForOrderMakingModel.asStateFlow()

    private var listProductAvailabilityModel = listOf<ProductAvailabilityModel>()

    private val network = Network()

    private var userId = UNAUTHORIZED_USER

    private var city: String? = null

    private var arrayListIdsSelectedBasketModels: ArrayList<Int>? = null

    private var arrayListNumberProductsSelectedBasketModels: ArrayList<Int>? = null

    private val mutableListNumberProductsModel = mutableListOf<NumberProductsModel>()

    private var isInit = true

    private var isShownSendingRequests = true

    private var isShownFillData = true

    fun initValues(
        userId: Int,
        arrayListIdsSelectedBasketModels: ArrayList<Int>?,
        arrayListNumberProductsSelectedBasketModels: ArrayList<Int>?
    ){
        try {
            if (isInit){
                this.userId = userId
                this.arrayListIdsSelectedBasketModels = arrayListIdsSelectedBasketModels!!
                this.arrayListNumberProductsSelectedBasketModels = arrayListNumberProductsSelectedBasketModels!!

                for (i in 0..< arrayListIdsSelectedBasketModels.size){
                    val productId = arrayListIdsSelectedBasketModels[i]
                    val numberProducts = arrayListNumberProductsSelectedBasketModels[i]
                    mutableListNumberProductsModel.add(NumberProductsModel(
                        productId = productId,
                        numberProducts = numberProducts
                    ))
                }
            }
            isInit = false
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests){

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    onLoading()

                    val getProductAvailabilityUseCase = GetProductAvailabilityByIdsProductsUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        listIdsProducts = arrayListIdsSelectedBasketModels?: arrayListOf()
                    )

                    val getPharmacyAddressesUseCase = GetPharmacyAddressesUseCase(
                        catalogRepository = catalogRepositoryImpl
                    )

                    val getCityByUserIdUseCase = GetCityByUserIdUseCase(
                        profileRepository = profileRepositoryImpl,
                        userId = userId
                    )

                    viewModelScope.launch {
                        try {
                            val resultGetProductAvailability = getProductAvailabilityUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS,
                                    result = result
                                )
                            }

                            val resultGetPharmacyAddresses = getPharmacyAddressesUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_PHARMACY_ADDRESSES,
                                    result = result
                                )
                            }

                            val resultGetCityByUserId = getCityByUserIdUseCase.execute().map { result ->
                                return@map RequestModel(
                                    type = TYPE_GET_CITY_BY_USER_ID,
                                    result = result
                                )
                            }

                            val combineFlow = combine(
                                resultGetProductAvailability,
                                resultGetPharmacyAddresses,
                                resultGetCityByUserId
                            ){ getProductAvailability, getPharmacyAddresses, getCityByUserId ->
                                return@combine listOf(
                                    getProductAvailability,
                                    getPharmacyAddresses,
                                    getCityByUserId
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
                        catch (e: Exception){
                            Log.e("TAG",e.stackTraceToString())
                            _stateScreen.value = Result.Error(exception = e)
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
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listProductAvailabilityModel: List<ProductAvailabilityModel>,
        listPharmacyAddressesModel: List<PharmacyAddressesModel>,
        city: String
    ){
        if (isShownFillData){

            this.listProductAvailabilityModel = listProductAvailabilityModel
            this.city = city

            val mutableListAvailabilityProductsForOrderMakingModel = mutableListOf<AvailabilityProductsForOrderMakingModel>()

            val mapProductAvailabilityModel = listProductAvailabilityModel.groupBy { it.addressId }

            listPharmacyAddressesModel.forEach { pharmacyAddressesModel ->

                val mutableListCurrentNumberProducts = mutableListOf<NumberProductsModel>()
                mapProductAvailabilityModel.forEach { (addressId, listProductAvailabilityModel) ->
                    if (addressId == pharmacyAddressesModel.addressId) {

                        listProductAvailabilityModel.forEach { productAvailabilityModel ->

                            mutableListNumberProductsModel.forEach { numberProductsModel ->
                                if (numberProductsModel.productId == productAvailabilityModel.productId){
                                    if (numberProductsModel.numberProducts <= productAvailabilityModel.numberProducts){
                                        mutableListCurrentNumberProducts.add(numberProductsModel)
                                    }
                                }
                            }
                        }

                    }
                }
                val availableQuantity = mutableListCurrentNumberProducts.sumOf { it.numberProducts }

                val currentCity = pharmacyAddressesModel.city.substringAfterLast(' ')
                if (currentCity == city) {
                    mutableListAvailabilityProductsForOrderMakingModel.add(AvailabilityProductsForOrderMakingModel(
                        addressId = pharmacyAddressesModel.addressId,
                        address = pharmacyAddressesModel.address,
                        city = pharmacyAddressesModel.city,
                        availableQuantity = availableQuantity
                    ))
                }
            }

            _listAvailabilityProductsForOrderMakingModel.value = mutableListAvailabilityProductsForOrderMakingModel

        }
        isShownFillData = false
    }

    fun installAdapter(block: (Int) -> Unit){
        block(mutableListNumberProductsModel.sumOf { it.numberProducts })
    }

    fun navigateOnMap(navigate: (String,ArrayList<Int>,ArrayList<Int>) -> Unit){
        try {
            navigate(
                FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING,
                arrayListIdsSelectedBasketModels!!,
                arrayListNumberProductsSelectedBasketModels!!
            )
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun chooseAddress(block: (ArrayList<Int>,ArrayList<Int>) -> Unit){
        try {
            block(arrayListIdsSelectedBasketModels!!,arrayListNumberProductsSelectedBasketModels!!)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

}





