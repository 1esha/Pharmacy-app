package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.profile.ProfileRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.catalog.usecases.GetOperatingModeUseCase
import com.example.domain.catalog.usecases.GetPharmacyAddressesDetailsUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.usecases.GetCityByUserIdUseCase
import com.example.pharmacyapp.KEY_OPERATING_MODE
import com.example.pharmacyapp.TYPE_GET_CITY_BY_USER_ID
import com.example.pharmacyapp.TYPE_GET_OPERATING_MODE
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES_DETAILS
import kotlinx.coroutines.launch

/**
 * Класс [MapViewModel] является viewModel для класса MapFragment.
 */
class MapViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val profileRepositoryImpl: ProfileRepositoryImpl,
    private val catalogRepositoryImpl: CatalogRepositoryImpl
    ): ViewModel() {

        companion object {
            // Ключи для передачи и получения значения был ли обработан запрос или нет

            private const val KEY_IS_SHOWN_GET_CITY_BY_USER_ID = "KEY_IS_SHOWN_GET_CITY_BY_USER_ID"

            private const val KEY_IS_SHOWN_GET_PHARMACY_ADDRESSES_DETAILS = "KEY_IS_SHOWN_GET_PHARMACY_ADDRESSES_DETAILS"

            private const val KEY_IS_SHOWN_GET_OPERATING_MODE = "KEY_IS_SHOWN_GET_OPERATING_MODE"
        }

    /**
     * LiveDate, наблюдающие за изменениями дргих объектов LiveDate, которые являются их источниками.
     *
     * [mediatorMap] - наблюдает за изменениями [resultGetCityByUserId],[resultGetPharmacyAddressesDetails],[resultGetOperatingMode];
     * [mediatorIsAllRequests]- наблюдает за изменениями [_listOperatingMode],[_lisAllPharmacyAddressesDetails],[_city].
     */
    val mediatorMap = MediatorLiveData<MediatorResultsModel<*>>()

    val mediatorIsAllRequests = MediatorLiveData<Any>()

    /**
     * Переменные [MutableLiveData], хранящие результаты запросов - [resultGetCityByUserId],[resultGetPharmacyAddressesDetails],[resultGetOperatingMode].
     */
    private val resultGetCityByUserId = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<String>>>>()

    private val resultGetPharmacyAddressesDetails = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<PharmacyAddressesDetailsModel>?>>>>()

    private val resultGetOperatingMode = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<OperatingModeModel>?>>>>()

    /**
     * Переменные [MutableLiveData], хранящие значения рузультатов запросов.
     *
     * [city] - хранит город пользователя;
     * [listAllPharmacyAddressesDetails] - хранит список подробной информации обо всех аптеках;
     * [listOperatingMode] - хранит список режимов работы.
     */
    private val _city = MutableLiveData<String>()
    val city: LiveData<String> = _city

    private val _lisAllPharmacyAddressesDetails = MutableLiveData<List<PharmacyAddressesDetailsModel>>(emptyList())
    val listAllPharmacyAddressesDetails: LiveData<List<PharmacyAddressesDetailsModel>> = _lisAllPharmacyAddressesDetails

    private val _listOperatingMode = MutableLiveData<List<OperatingModeModel>>(emptyList())
    val listOperatingMode: LiveData<List<OperatingModeModel>> = _listOperatingMode

    /**
     * Переменные:
     * [isShownGetCityUserId],
     * [isShownGetPharmacyAddressesDetails],
     * [isShownGetOperatingMode],
     * хранят значения соответствующих запросов был ли обработан запрос или нет.
     */
    val isShownGetCityUserId: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_CITY_BY_USER_ID] ?: false

    val isShownGetPharmacyAddressesDetails: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PHARMACY_ADDRESSES_DETAILS] ?: false

    val isShownGetOperatingMode: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_OPERATING_MODE] ?: false

    /**
     * Переменная [errorType] хранит тип ошибки. По умолчанию является - [OtherError].
     */
    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    /**
     * Установка источников наблюдения для [mediatorMap] и [mediatorIsAllRequests] при инициализации класса.
     */
    init {
        mediatorMap.addSource(resultGetCityByUserId) { result ->
            mediatorMap.value = result
        }

        mediatorMap.addSource(resultGetPharmacyAddressesDetails) { result ->
            mediatorMap.value = result
        }

        mediatorMap.addSource(resultGetOperatingMode) { result ->
            mediatorMap.value = result
        }


        mediatorIsAllRequests.addSource(_listOperatingMode) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_lisAllPharmacyAddressesDetails) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_city) { result ->
            mediatorIsAllRequests.value = result
        }
    }

    /**
     * Получение города, который выбрал пользователь, по идентификатору пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    fun getCityByUserId(userId: Int) {
        val getCityByUserIdUseCase = GetCityByUserIdUseCase(
            profileRepository = profileRepositoryImpl,
            userId = userId
        )

        viewModelScope.launch {
            val result = getCityByUserIdUseCase.execute()

            resultGetCityByUserId.value = MediatorResultsModel(
                type = TYPE_GET_CITY_BY_USER_ID,
                result = result
            )
        }
    }

    /**
     * Полученние списка подробной информации об аптеках.
     */
    fun getPharmacyAddressesDetails() {
        val getPharmacyAddressesDetailsUseCase = GetPharmacyAddressesDetailsUseCase(
            catalogRepository = catalogRepositoryImpl
        )

        viewModelScope.launch {
            val result = getPharmacyAddressesDetailsUseCase.execute()

            resultGetPharmacyAddressesDetails.value = MediatorResultsModel(
                type = TYPE_GET_PHARMACY_ADDRESSES_DETAILS,
                result = result
            )
        }
    }

    /**
     * Получение списка режимов работы аптек.
     */
    fun getOperatingMode() {
        val getOperatingModeUseCase = GetOperatingModeUseCase(
            catalogRepository = catalogRepositoryImpl
        )

        viewModelScope.launch {
            val result = getOperatingModeUseCase.execute()

            resultGetOperatingMode.value = MediatorResultsModel(
                type = TYPE_GET_OPERATING_MODE,
                result = result
            )
        }
    }

    /**
     * Установка результата для [resultGetCityByUserId]
     */
    fun setResultGetCityByUserId(result: Result<ResponseValueModel<String>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("MapViewModel setResult errorType = null")
        }
        resultGetCityByUserId.value = MediatorResultsModel(
            type = TYPE_GET_CITY_BY_USER_ID,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetPharmacyAddressesDetails]
     */
    fun setResultGetPharmacyAddressesDetails(result: Result<ResponseValueModel<List<PharmacyAddressesDetailsModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("MapViewModel setResult errorType = null")
        }
        resultGetPharmacyAddressesDetails.value = MediatorResultsModel(
            type = TYPE_GET_PHARMACY_ADDRESSES_DETAILS,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetOperatingMode]
     */
    fun setResultGetListOperatingMode(result: Result<ResponseValueModel<List<OperatingModeModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("MapViewModel setResult errorType = null")
        }
        resultGetOperatingMode.value = MediatorResultsModel(
            type = KEY_OPERATING_MODE,
            result = result
        )
    }

    /**
     * Установка города.
     *
     * Параметры:
     * [city] - город.
     */
    fun setCity(city: String) {
        _city.value = city
    }

    /**
     * Установка значения для списка подробной информации обо всех аптеках.
     *
     * Параметры:
     * [listAllPharmacyAddressesDetails] - список подробной информации обо всех аптеках
     */
    fun setListAllPharmacyAddressesDetails(listAllPharmacyAddressesDetails: List<PharmacyAddressesDetailsModel>){
        _lisAllPharmacyAddressesDetails.value = listAllPharmacyAddressesDetails
    }

    /**
     * Установка значения для списка режимов работы аптек.
     *
     * Параметры:
     * [listOperatingMode] - список режимов работы.
     */
    fun setListOperatingMode(listOperatingMode: List<OperatingModeModel>){
        _listOperatingMode.value = listOperatingMode
    }

    /**
     * Функции [setIsShownGetCityUserId],[setIsShownGetPharmacyAddressesDetails],[setIsShownGetOperatingMode]
     * устанавливают значения для определения был ли обработан запрос или нет.
     *
     * Параметры:
     * isShown - true запрос обработан, false запрос надо обработать.
     */
    fun setIsShownGetCityUserId(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_CITY_BY_USER_ID] = isShown
    }

    fun setIsShownGetPharmacyAddressesDetails(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_PHARMACY_ADDRESSES_DETAILS] = isShown
    }

    fun setIsShownGetOperatingMode(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_OPERATING_MODE] = isShown
    }

    /**
     * Отчистка типа ошибки.
     */
    fun clearErrorType() {
        _errorType.value = OtherError()
    }
}