package com.example.pharmacyapp.main

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.data.NOT_SELECTED
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ALL_PHARMACIES
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_AVAILABILITY_PHARMACY_ADDRESSES_DETAILS
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_CITY_BY_USER_ID
import com.example.pharmacyapp.TYPE_GET_OPERATING_MODE
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES_DETAILS
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentMapBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.MapViewModel
import com.example.pharmacyapp.main.viewmodels.factories.MapViewModelFactory
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import java.lang.Exception
import kotlin.properties.Delegates

/**
 * Класс MapFragment отвечает за работу с картой местности.
 * На карте маркерами указаны аптеки.
 * В зависимости от передаваемого флага логика отрисовки маркеров меняется:
 * [FLAG_ALL_PHARMACIES] для отрисовки карты со всеми аптеками;
 * [FLAG_CURRENT_PRODUCT] для отрисовки карты текущего товара с наличием его в аптеках.
 */
class MapFragment : Fragment(), ProfileResult {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val mapViewModel: MapViewModel by viewModels(
        factoryProducer = { MapViewModelFactory() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private var userId by Delegates.notNull<Int>()

    private var flag by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        // получение флага для отрисовки карты
        flag = arguments?.getString(KEY_FLAGS_FOR_MAP) ?: FLAG_ALL_PHARMACIES

        val isShownGetCityUserId = mapViewModel.isShownGetCityUserId
        val isShownGetPharmacyAddressesDetails = mapViewModel.isShownGetPharmacyAddressesDetails
        val isShownGetOperatingMode = mapViewModel.isShownGetOperatingMode

        if (!isShownGetCityUserId) {
            onSuccessfulEvent(type = TYPE_GET_CITY_BY_USER_ID) {
                mapViewModel.getCityByUserId(userId = userId)
            }
        }

        if (!isShownGetPharmacyAddressesDetails) {
            onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES_DETAILS){
                mapViewModel.getPharmacyAddressesDetails()
            }
        }

        if (!isShownGetOperatingMode) {
            onSuccessfulEvent(type = TYPE_GET_OPERATING_MODE) {
                mapViewModel.getOperatingMode()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        // Обработка кнопки "Прпробовать снова"
        layoutPendingResultMap.bTryAgain.setOnClickListener {
            // Повторный вызов всех запросов
            with(mapViewModel) {
                onSuccessfulEvent(type = TYPE_GET_CITY_BY_USER_ID) {
                    setIsShownGetCityUserId(isShown = false)
                    getCityByUserId(userId = userId)
                }

                onSuccessfulEvent(type = TYPE_GET_PHARMACY_ADDRESSES_DETAILS){
                    setIsShownGetPharmacyAddressesDetails(isShown = false)
                    getPharmacyAddressesDetails()
                }

                onSuccessfulEvent(type = TYPE_GET_OPERATING_MODE) {
                    setIsShownGetOperatingMode(isShown = false)
                    getOperatingMode()
                }
            }

        }

        mapViewModel.mediatorIsAllRequests.observe(viewLifecycleOwner) {

            val listOperatingMode = mapViewModel.listOperatingMode.value ?: emptyList()
            val listAllPharmacyAddressesDetails = mapViewModel.listAllPharmacyAddressesDetails.value ?: emptyList()
            val city = mapViewModel.city.value ?: NOT_SELECTED

            // Если результаты по всем запросоам пришли
            if (
                listOperatingMode.isNotEmpty() &&
                listAllPharmacyAddressesDetails.isNotEmpty() &&
                city != NOT_SELECTED
                ) {

                // Настройка карты
                installCity(city = city)

                // Установка маркеров
                installMarkers(
                    listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails,
                    listOperatingMode = listOperatingMode
                )

                mapView.visibility = View.VISIBLE

                updateUI(flag = FLAG_SUCCESS_RESULT)
            }
        }

        mapViewModel.mediatorMap.observe(viewLifecycleOwner) { mediatorResult ->

            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    onSuccessResultListener(
                        userId = userId,
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = mapViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) = with(binding) {
        when(type) {
            TYPE_GET_CITY_BY_USER_ID -> {
                Log.i("TAG","MapFragment onSuccessResultListener TYPE_GET_CITY_BY_USER_ID")
                val isShownGetCityUserId = mapViewModel.isShownGetCityUserId

                if (!isShownGetCityUserId) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {
                        Log.i("TAG","TYPE_GET_CITY_BY_USER_ID OK")
                        val city = responseValueModel.value as String
                        mapViewModel.setCity(city = city)

                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        mapViewModel.setResultGetCityByUserId(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                mapViewModel.setIsShownGetCityUserId(isShown = true)
            }
            TYPE_GET_PHARMACY_ADDRESSES_DETAILS -> {
                Log.i("TAG","MapFragment onSuccessResultListener TYPE_GET_PHARMACY_ADDRESSES_DETAILS")
                val isShownGetPharmacyAddressesDetails = mapViewModel.isShownGetPharmacyAddressesDetails

                if (!isShownGetPharmacyAddressesDetails) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {
                        Log.i("TAG","TYPE_GET_PHARMACY_ADDRESSES_DETAILS OK")

                        val _listAllPharmacyAddressesDetails = responseValueModel.value as List<*>

                        val listAllPharmacyAddressesDetails = _listAllPharmacyAddressesDetails.map {
                            return@map it as PharmacyAddressesDetailsModel
                        }
                        mapViewModel.setListAllPharmacyAddressesDetails(listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails)

                    }
                    else {
                        mapViewModel.setResultGetPharmacyAddressesDetails(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                mapViewModel.setIsShownGetPharmacyAddressesDetails(isShown = true)
            }
            TYPE_GET_OPERATING_MODE -> {
                Log.i("TAG","MapFragment onSuccessResultListener TYPE_GET_OPERATING_MODE")

                val isShownGetOperatingMode = mapViewModel.isShownGetOperatingMode

                if (!isShownGetOperatingMode) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val _listOperatingMode = responseValueModel.value as List<*>

                        val listOperatingMode = _listOperatingMode.map {
                            return@map it as OperatingModeModel
                        }

                        mapViewModel.setListOperatingMode(listOperatingMode = listOperatingMode)

                    }
                    else {
                        mapViewModel.setResultGetListOperatingMode(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                mapViewModel.setIsShownGetOperatingMode(isShown = true)
            }
        }
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        mapViewModel.setIsShownGetCityUserId(isShown = true)
        mapViewModel.setIsShownGetPharmacyAddressesDetails(isShown = true)
        mapViewModel.setIsShownGetOperatingMode(isShown = true)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
    }

    override fun onPendingResultListener() {
        mapViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun onSuccessfulEvent(
        type: String,
        exception: Exception?,
        onSuccessfulEventListener: () -> Unit
    ) {
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                when(type) {
                    TYPE_GET_CITY_BY_USER_ID -> mapViewModel.setResultGetCityByUserId(result = PendingResult())
                    TYPE_GET_PHARMACY_ADDRESSES_DETAILS -> mapViewModel.setResultGetPharmacyAddressesDetails(result = PendingResult())
                    TYPE_GET_OPERATING_MODE -> mapViewModel.setResultGetListOperatingMode(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                when(type) {
                    TYPE_GET_CITY_BY_USER_ID -> mapViewModel.setResultGetCityByUserId(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_PHARMACY_ADDRESSES_DETAILS -> mapViewModel.setResultGetPharmacyAddressesDetails(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_OPERATING_MODE -> mapViewModel.setResultGetListOperatingMode(result = ErrorResult(exception = currentException), errorType = errorType)
                }
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultMap) {
        when(flag) {
            FLAG_PENDING_RESULT -> {
                Log.i("TAG","FLAG_PENDING_RESULT")
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.VISIBLE
            }
            FLAG_SUCCESS_RESULT -> {
                Log.i("TAG","FLAG_SUCCESS_RESULT")
                root.visibility = View.GONE
                bTryAgain.visibility = View.INVISIBLE
                tvErrorMessage.visibility = View.INVISIBLE
                progressBar.visibility = View.INVISIBLE
            }
            FLAG_ERROR_RESULT -> {
                Log.i("TAG","FLAG_ERROR_RESULT")
                root.visibility = View.VISIBLE
                bTryAgain.visibility = View.VISIBLE
                tvErrorMessage.visibility = View.VISIBLE
                tvErrorMessage.text = messageError
                progressBar.visibility = View.INVISIBLE
            }
        }
    }

    /**
     * Установка города к которому будет начальное приближение.
     *
     * Параметры:
     * [city] - город, выбранный пользователем.
     */
    private fun installCity(city: String) = with(binding) {

        Configuration.getInstance().userAgentValue = getString(R.string.app_name)

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.minZoomLevel = 4.0

        val controller = mapView.controller

        // Установка начальных данных карты в зависимости от города
        if (city == NOT_SELECTED) {
            controller.setZoom(4.0)

            val defaultGeoPoint = GeoPoint(61.19, 92.81)
            controller.setCenter(defaultGeoPoint)
        }
        else {
            val centerGeoPoint = getStartPositionByCity(city = city)

            controller.setZoom(13.5)
            controller.setCenter(centerGeoPoint)
        }
    }

    /**
     * Полученени стартовых координат в зависимости от города.
     *
     * Параметры:
     * [city] - город, выбранный пользователем.
     */
    private fun getStartPositionByCity(city: String): GeoPoint {

        return when(city) {

            getString(R.string.novocheboksarsk) -> GeoPoint(56.1156,47.4961)

            getString(R.string.cheboksary) -> GeoPoint(56.1322,47.2519)

            else -> throw IllegalArgumentException("MapFragment некорректные данные для получения GeoPoint")

        }
    }

    /**
     * Установка маркеров на карту в зависимости от флаг.
     *
     * Параметры:
     * [listAllPharmacyAddressesDetails] - список подробной информации о всех аптеках.
     * [listOperatingMode] - список режимов работы
     */
    private fun installMarkers(
        listAllPharmacyAddressesDetails: List<PharmacyAddressesDetailsModel>,
        listOperatingMode: List<OperatingModeModel>
        ) = with(binding) {

        val controller = mapView.controller

        // Список идентификаторов аптек с наличием товара
        val arrayListIdsAvailabilityPharmacyAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_IDS_AVAILABILITY_PHARMACY_ADDRESSES_DETAILS)?:
        throw NullPointerException("MapFragment arrayListIdsAvailabilityPharmacyAddresses = null")

        // Получаем список содержащий только адреса аптек в которых товар в наличии
        val listPharmacyAddressesDetails = listAllPharmacyAddressesDetails.filter {
            arrayListIdsAvailabilityPharmacyAddresses.contains(it.pharmacyAddressesModel.addressId)
        }

        listAllPharmacyAddressesDetails.forEach { pharmacyAddressesDetailsModel ->

            // Значение в наличии ли текущий товар
            val isAvailability = arrayListIdsAvailabilityPharmacyAddresses.contains(
                pharmacyAddressesDetailsModel.pharmacyAddressesModel.addressId
            )

            val marker = Marker(mapView)

            val geoPoint = GeoPoint(
                pharmacyAddressesDetailsModel.latitude,
                pharmacyAddressesDetailsModel.longitude
            )

            // Установка координат для маркера
            marker.position = geoPoint

            // Получение изображения маркера в зависимости от того в наличии ли товар
            val drawable = ContextCompat.getDrawable(
                requireContext(),
                if (isAvailability) R.drawable.ic_location else R.drawable.ic_location_lignt
            )

            marker.icon = drawable
            marker.title = pharmacyAddressesDetailsModel.pharmacyAddressesModel.address
            marker.setOnMarkerClickListener { marker, mapView ->

                // Установка анимации при нажатии на маркер
                controller.animateTo(geoPoint,18.0,1500L)

                onClickMarker(
                    pharmacyAddressesDetailsModel = pharmacyAddressesDetailsModel,
                    listOperatingMode = listOperatingMode,
                    isAvailability = isAvailability,
                )

                true
            }

            // Добавление маркера на карту
            mapView.overlays.add(marker)

        }

    }

    /**
     * Обработка нажатия на маркер. Открывает нижнюю панель с информацией об аптеке.
     *
     * Параметры:
     * [pharmacyAddressesDetailsModel] - модель подробной информации об аптеке;
     * [listOperatingMode] - список режимов работы;
     * [isAvailability] - значение в наличии ли в текущей аптеке товар.
     */
    private fun onClickMarker(
        pharmacyAddressesDetailsModel: PharmacyAddressesDetailsModel,
        listOperatingMode: List<OperatingModeModel>,
        isAvailability: Boolean
    ) = with(PharmacyDetailsBottomSheetDialogFragment){

        // получение списка времени со скольки начинает работать аптека
        val arrayListOperatingModesTimeFrom = listOperatingMode.toArrayListString(
            modeId = pharmacyAddressesDetailsModel.modeId,
            flag = FLAG_TIME_FROM
        )

        // получение списка времени до скольки работает аптека
        val arrayListOperatingModesTimeBefore = listOperatingMode.toArrayListString(
            modeId = pharmacyAddressesDetailsModel.modeId,
            flag = FLAG_TIME_BEFORE
        )

        val pharmacyDetailsBottomSheetDialogFragment = newInstance(
            pharmacyAddressesDetailsModel = pharmacyAddressesDetailsModel,
            arrayListOperatingModesTimeFrom = arrayListOperatingModesTimeFrom,
            arrayListOperatingModesTimeBefore = arrayListOperatingModesTimeBefore,
            isAvailability = isAvailability,
            flag = flag
        )

        pharmacyDetailsBottomSheetDialogFragment.show(parentFragmentManager, TAG_PHARMACY_DETAILS_BOTTOM_SHEET)
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
    }

}