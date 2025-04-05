package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.NOT_SELECTED
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.usecases.GetOperatingModeUseCase
import com.example.domain.catalog.usecases.GetPharmacyAddressesDetailsUseCase
import com.example.domain.catalog.usecases.GetProductAvailabilityByIdsProductsUseCase
import com.example.domain.models.NumberProductsModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.usecases.GetCityByUserIdUseCase
import com.example.pharmacyapp.FLAG_ALL_PHARMACIES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_CITY_BY_USER_ID
import com.example.pharmacyapp.TYPE_GET_OPERATING_MODE
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES_DETAILS
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS
import com.example.pharmacyapp.UNAUTHORIZED_USER
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

/**
 * Класс [MapViewModel] является viewModel для класса MapFragment.
 */
class MapViewModel(
    private val profileRepositoryImpl: ProfileRepositoryImpl,
    private val catalogRepositoryImpl: CatalogRepositoryImpl
    ): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val _city = MutableStateFlow<String>(NOT_SELECTED)
    val city = _city.asStateFlow()

    private val _lisAllPharmacyAddressesDetails = MutableStateFlow<List<PharmacyAddressesDetailsModel>>(emptyList())
    val listAllPharmacyAddressesDetails = _lisAllPharmacyAddressesDetails.asStateFlow()

    private var listOperatingMode = listOf<OperatingModeModel>()

    private var listProductAvailabilityModel: List<ProductAvailabilityModel> = emptyList()

    private val mutableListNumberProductsModel = mutableListOf<NumberProductsModel>()

    private var userId = UNAUTHORIZED_USER

    private var flag = FLAG_ALL_PHARMACIES

    private val network = Network()

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isInit = true

    fun initValues(
        userId: Int,
        flag: String,

        arrayListIdsProducts: ArrayList<Int>?,
        arrayListNumberProducts: ArrayList<Int>?
    ){
        try {
            if (isInit) {
                this.userId = userId
                this.flag = flag

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

                isInit = false
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests) {

            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {
                    onLoading()

                    val getCityByUserIdUseCase = GetCityByUserIdUseCase(
                        profileRepository = profileRepositoryImpl,
                        userId = userId
                    )
                    val getPharmacyAddressesDetailsUseCase = GetPharmacyAddressesDetailsUseCase(
                        catalogRepository = catalogRepositoryImpl
                    )
                    val getOperatingModeUseCase = GetOperatingModeUseCase(
                        catalogRepository = catalogRepositoryImpl
                    )
                    val listIdsProducts = mutableListNumberProductsModel.map { it.productId }
                    val getProductAvailability = GetProductAvailabilityByIdsProductsUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        listIdsProducts = listIdsProducts
                    )
                    viewModelScope.launch {
                        val resultGetCityByUserId = getCityByUserIdUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_CITY_BY_USER_ID,
                                result = result
                            )
                        }

                        val resultGetPharmacyAddressesDetails = getPharmacyAddressesDetailsUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PHARMACY_ADDRESSES_DETAILS,
                                result = result
                            )
                        }

                        val resultGetOperatingMode = getOperatingModeUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_OPERATING_MODE,
                                result = result
                            )
                        }

                        val resultGetProductAvailability = getProductAvailability.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS,
                                result = result
                            )
                        }

                        val combinedFlow = combine(
                            resultGetCityByUserId,
                            resultGetPharmacyAddressesDetails,
                            resultGetOperatingMode,
                            resultGetProductAvailability
                        ) { cityByUserId, pharmacyAddressesDetail, operatingMode, getProductAvailability ->

                            return@combine listOf(
                                cityByUserId,
                                pharmacyAddressesDetail,
                                operatingMode,
                                getProductAvailability
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
            isShownSendingRequests = false
        }

    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        city: String,
        listAllPharmacyAddressesDetails: List<PharmacyAddressesDetailsModel>,
        listOperatingMode: List<OperatingModeModel>,
        listProductAvailabilityModel: List<ProductAvailabilityModel>
    ){
        if (isShownFillData){
            _city.value = city
            _lisAllPharmacyAddressesDetails.value = listAllPharmacyAddressesDetails
            this.listOperatingMode = listOperatingMode

            this.listProductAvailabilityModel = listProductAvailabilityModel
        }

        isShownFillData = false
    }

    fun installCity(
        city: String,
        novocheboksarsk: String,
        cheboksary: String,
        block: (Double,GeoPoint) -> Unit
    ){
        // Установка начальных данных карты в зависимости от города
        if (city == NOT_SELECTED) {

            val defaultGeoPoint = GeoPoint(61.19, 92.81)

            block(4.0,defaultGeoPoint)
        }
        else {
            val centerGeoPoint = getStartPositionByCity(
                city = city,
                novocheboksarsk = novocheboksarsk,
                cheboksary = cheboksary
            )

            block(13.5,centerGeoPoint)
        }
    }

    fun installMarkers(
        listAllPharmacyAddressesDetails: List<PharmacyAddressesDetailsModel>,
        block: (Int,Int,GeoPoint,PharmacyAddressesDetailsModel,Int?,Int?) -> Unit
        ){
        listAllPharmacyAddressesDetails.forEach { pharmacyAddressesDetailsModel ->
            try {
                val geoPoint = GeoPoint(
                    pharmacyAddressesDetailsModel.latitude,
                    pharmacyAddressesDetailsModel.longitude
                )

                when(flag){
                    FLAG_ALL_PHARMACIES -> {
                        block(FLAG_IN_STOCK,R.drawable.ic_location,geoPoint,pharmacyAddressesDetailsModel,null,null)
                    }
                    else -> {
                        val mapProductAvailabilityModel = listProductAvailabilityModel.groupBy { it.addressId }

                        val mutableListCurrentNumberProducts = mutableListOf<NumberProductsModel>()

                        mapProductAvailabilityModel.forEach { (addressId, listProductAvailabilityModel) ->
                            if (addressId == pharmacyAddressesDetailsModel.pharmacyAddressesModel.addressId) {

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

                        val totalNumber = mutableListNumberProductsModel.sumOf { it.numberProducts }

                        val flagStatus = when{
                            totalNumber == availableQuantity -> FLAG_IN_STOCK
                            availableQuantity in 1..< totalNumber -> FLAG_WARNING
                            availableQuantity == 0 -> FLAG_OUT_OF_STOCK
                            else -> throw IllegalArgumentException()
                        }

                        val drawable = when (flagStatus){
                            FLAG_IN_STOCK -> R.drawable.ic_location
                            FLAG_OUT_OF_STOCK -> R.drawable.ic_location_out_of_stock
                            FLAG_WARNING -> R.drawable.ic_location_warning
                            else -> throw IllegalArgumentException()
                        }

                        block(flagStatus,drawable,geoPoint,pharmacyAddressesDetailsModel,totalNumber,availableQuantity)
                    }
                }
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
            }
        }
    }

    fun onClickMarker(modeId: Int,block: (ArrayList<String>,ArrayList<String>,String) -> Unit){
        // получение списка времени со скольки начинает работать аптека
        val arrayListOperatingModesTimeFrom = listOperatingMode.toArrayListString(
            modeId = modeId,
            flag = FLAG_TIME_FROM
        )

        // получение списка времени до скольки работает аптека
        val arrayListOperatingModesTimeBefore = listOperatingMode.toArrayListString(
            modeId = modeId,
            flag = FLAG_TIME_BEFORE
        )

        block(arrayListOperatingModesTimeFrom,arrayListOperatingModesTimeBefore,flag)
    }

    /**
     * Полученени стартовых координат в зависимости от города.
     *
     * Параметры:
     * [city] - город, выбранный пользователем.
     */
    private fun getStartPositionByCity(
        city: String,
        novocheboksarsk: String,
        cheboksary: String
    ): GeoPoint {

        return when(city) {

            novocheboksarsk -> GeoPoint(56.1156,47.4961)

            cheboksary -> GeoPoint(56.1322,47.2519)

            else -> throw IllegalArgumentException("MapFragment некорректные данные для получения GeoPoint")

        }
    }

    /**
     * Преобразование списка всех режимов работы List<OperatingModeModel> в ArrayList<String>.
     *
     * Параметры:
     * [modeId] - идентфикатор режима работы, служит для получения списка всех дней недели с этим режимом работы.
     * [flag] - служит для определения возращаемого списка.
     * [FLAG_TIME_FROM] - вернет список с временем начала работы;
     * [FLAG_TIME_BEFORE] - вернет список с временем окончания работы.
     */
    private fun List<OperatingModeModel>.toArrayListString(modeId: Int, flag: Boolean): ArrayList<String> {

        val arrayListOperatingModesTimeFrom = arrayListOf<String>()
        val arrayListOperatingModesTimeBefore = arrayListOf<String>()

        this.forEach { operatingModeModel ->
            if (operatingModeModel.modeId == modeId) {
                arrayListOperatingModesTimeFrom.add(operatingModeModel.timeFrom)
                arrayListOperatingModesTimeBefore.add(operatingModeModel.timeBefore)
            }
        }

        return when(flag) {

            FLAG_TIME_FROM -> arrayListOperatingModesTimeFrom

            FLAG_TIME_BEFORE -> arrayListOperatingModesTimeBefore

            else -> throw IllegalArgumentException("MapFragment некорректные данные")

        }
    }


    companion object {
        // Флаг для получения списка начала времени работы
        private const val FLAG_TIME_FROM = true

        // Флаг для получения списка окончания времени работы
        private const val FLAG_TIME_BEFORE = false

        const val FLAG_IN_STOCK = 1
        const val FLAG_OUT_OF_STOCK = 2
        const val FLAG_WARNING = 3
    }

}