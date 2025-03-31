package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.catalog.usecases.GetPharmacyAddressesUseCase
import com.example.domain.catalog.usecases.GetProductAvailabilityByPathUseCase
import com.example.domain.models.RequestModel
import com.example.domain.models.SelectedPharmacyAddressesModel
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH
import com.example.pharmacyapp.toArrayListInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PharmacyAddressesViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl
) : ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private val network = Network()

    private var isShownSendingRequests = true

    private var isShownFillingList = true

    private var isInit = true

    private var isInstallAdapter = true

    private val _counter = MutableStateFlow<Int>(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _listSelectedPharmacyAddresses = MutableStateFlow<List<SelectedPharmacyAddressesModel>>(mutableListOf())
    val listSelectedPharmacyAddresses: StateFlow<List<SelectedPharmacyAddressesModel>> = _listSelectedPharmacyAddresses.asStateFlow()

    private var arrayListSelectedIdsAddresses = arrayListOf<Int>()

    private var path = EMPTY_STRING


    fun initValues(
        path: String?,
        arrayListSelectedIdsAddresses: ArrayList<Int>
    ){
        try {
            if (isInit) {
                this.path = path!!
                this.arrayListSelectedIdsAddresses = arrayListSelectedIdsAddresses

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

                    if (path == EMPTY_STRING) {
                        _stateScreen.value = Result.Error(exception = NullPointerException())
                        return@checkNetworkStatus
                    }

                    val getPharmacyAddressesUseCase = GetPharmacyAddressesUseCase(
                        catalogRepository = catalogRepositoryImpl
                    )
                    val getProductAvailabilityByPathUseCase = GetProductAvailabilityByPathUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        path = path
                    )

                    viewModelScope.launch {
                        val resultGetPharmacyAddresses = getPharmacyAddressesUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PHARMACY_ADDRESSES,
                                result = result
                            )
                        }

                        val resultGetProductAvailabilityByPath = getProductAvailabilityByPathUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH,
                                result = result
                            )
                        }

                        val combinedFlow = combine(
                            resultGetPharmacyAddresses,
                            resultGetProductAvailabilityByPath) { getPharmacyAddresses, getProductAvailabilityByPath ->

                            return@combine listOf(
                                getPharmacyAddresses,
                                getProductAvailabilityByPath
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
        isShownFillingList = true
        isInstallAdapter = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillingList(
        listPharmacyAddresses: List<PharmacyAddressesModel>,
        listProductAvailability: List<ProductAvailabilityModel>
    ) {
        if (isShownFillingList) {
            val mutableListSelectedPharmacyAddresses = mutableListOf<SelectedPharmacyAddressesModel>()

            listPharmacyAddresses.forEach { pharmacyAddress ->
                val isAvailabilityProduct = listProductAvailability.any { productAvailability ->
                    productAvailability.addressId == pharmacyAddress.addressId && productAvailability.numberProducts > 0
                }

                val isSelect = arrayListSelectedIdsAddresses.any { it == pharmacyAddress.addressId }

                if (isAvailabilityProduct) mutableListSelectedPharmacyAddresses.add(
                    SelectedPharmacyAddressesModel(
                        isSelected = isSelect,
                        pharmacyAddressesModel = pharmacyAddress
                    )
                )
            }

            _listSelectedPharmacyAddresses.value = mutableListSelectedPharmacyAddresses
        }

        isShownFillingList = false
    }

    fun updateCounter(){ _counter.value = _listSelectedPharmacyAddresses.value.count { it.isSelected } }

    fun clearAddressesSelected(){
        setIsInstallAdapter(isInstallAdapter = true)

        _listSelectedPharmacyAddresses.value = _listSelectedPharmacyAddresses.value.map { it.copy(isSelected = false) }
    }

    fun installAdapter(block:() -> Unit){

        if (isInstallAdapter) block()

        if (_listSelectedPharmacyAddresses.value.isNotEmpty()) isInstallAdapter = false
    }

    fun setIsInstallAdapter(isInstallAdapter: Boolean){
        this.isInstallAdapter = isInstallAdapter
    }

    fun transmittingArrayListSelectedIds(back: (ArrayList<Int>) -> Unit) {
        val mutableListOnlySelectedPharmacyAddresses = _listSelectedPharmacyAddresses.value.filter { it.isSelected }

        val arrayListPharmacyAddressesId = mutableListOnlySelectedPharmacyAddresses.map { it.pharmacyAddressesModel.addressId }.toArrayListInt()

        back(arrayListPharmacyAddressesId)
    }

    fun onSelectAddress(addressId: Int, isSelect: Boolean){
        try {
            val mutableListSelectedPharmacyAddresses = _listSelectedPharmacyAddresses.value.toMutableList()

            val selectedPharmacyAddressesModel = mutableListSelectedPharmacyAddresses.find { it.pharmacyAddressesModel.addressId == addressId }

            val index = mutableListSelectedPharmacyAddresses.indexOf(selectedPharmacyAddressesModel)

            mutableListSelectedPharmacyAddresses.removeAt(index)
            mutableListSelectedPharmacyAddresses.add(index, selectedPharmacyAddressesModel!!.copy(isSelected = isSelect))

            _listSelectedPharmacyAddresses.value = mutableListSelectedPharmacyAddresses
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }

    }

}