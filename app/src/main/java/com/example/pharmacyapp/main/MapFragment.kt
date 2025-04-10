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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.domain.Result
import com.example.domain.asSuccess
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ALL_PHARMACIES
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_GET_CITY_BY_USER_ID
import com.example.pharmacyapp.TYPE_GET_OPERATING_MODE
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES_DETAILS
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentMapBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.MapViewModel
import com.example.pharmacyapp.main.viewmodels.factories.MapViewModelFactory
import com.example.pharmacyapp.tabs.basket.ChooseAddressForOrderMakingFragment
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.overlay.Marker
import kotlin.Exception

/**
 * Класс MapFragment отвечает за работу с картой местности.
 * На карте маркерами указаны аптеки.
 * В зависимости от передаваемого флага логика отрисовки маркеров меняется:
 * [FLAG_ALL_PHARMACIES] для отрисовки карты со всеми аптеками;
 * [FLAG_CURRENT_PRODUCT] для отрисовки карты текущего товара с наличием его в аптеках;
 * [FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING] для отрисовки карты выбора аптеки для оформления заказа.
 */
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val mapViewModel: MapViewModel by viewModels(
        factoryProducer = { MapViewModelFactory() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(mapViewModel){

            sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

            getSupportActivity().setFragmentResultListener(KEY_RESULT_FROM_PHARMACY_DETAILS) { _, bundle ->
                getSupportActivity().setFragmentResult(ChooseAddressForOrderMakingFragment.KEY_RESULT_FROM_MAP,bundle)
                findNavController().navigateUp()
            }

            initValues(
                userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER),
                flag = arguments?.getString(KEY_FLAGS_FOR_MAP) ?: FLAG_ALL_PHARMACIES,
                arrayListIdsProducts = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP),
                arrayListNumberProducts = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP)
            )

            sendingRequests(isNetworkStatus = isNetworkStatus)

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    stateScreen.collect{ result ->
                        when(result){
                            is Result.Loading -> {
                                onLoadingResultListener()
                            }
                            is Result.Success<*> -> {
                                onSuccessResultListener(data = result.data)
                            }
                            is Result.Error -> {
                                onErrorResultListener(exception = result.exception)
                            }
                        }
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    city.collect{ city ->
                        installCity(city = city)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    listAllPharmacyAddressesDetails.collect{ listAllPharmacyAddressesDetails ->
                        installMarkers(listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails)
                    }
                }
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
            mapViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","MapFragment onSuccessResultListener")
        try {
            val _listRequests = data as List<*>
            val listRequests = _listRequests.map { request ->
                return@map request as RequestModel
            }
            Log.i("TAG","listRequests = $listRequests")

            var fullType = ""
            listRequests.forEach { request ->
                fullType += request.type
            }

            when(fullType){
                TYPE_GET_CITY_BY_USER_ID + TYPE_GET_PHARMACY_ADDRESSES_DETAILS + TYPE_GET_OPERATING_MODE + TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS -> {
                    val resultGetCityByUserId = listRequests.find { it.type == TYPE_GET_CITY_BY_USER_ID }?.result!!.asSuccess()!!
                    val resultGetPharmacyAddressesDetails = listRequests.find { it.type == TYPE_GET_PHARMACY_ADDRESSES_DETAILS }?.result!!.asSuccess()!!
                    val resultGetOperatingMode = listRequests.find { it.type == TYPE_GET_OPERATING_MODE }?.result!!.asSuccess()!!
                    val resultGetProductAvailability = listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS }?.result!!.asSuccess()!!

                    val responseGetCityByUserId = resultGetCityByUserId.data as ResponseValueModel<*>
                    val responseGetPharmacyAddressesDetails = resultGetPharmacyAddressesDetails.data as ResponseValueModel<*>
                    val responseGetOperatingMode = resultGetOperatingMode.data as ResponseValueModel<*>
                    val responseGetProductAvailability = resultGetProductAvailability.data as ResponseValueModel<*>

                    val city = responseGetCityByUserId.value as String

                    val _listAllPharmacyAddressesDetails = responseGetPharmacyAddressesDetails.value as List<*>
                    val listAllPharmacyAddressesDetails = _listAllPharmacyAddressesDetails.map { it as PharmacyAddressesDetailsModel }

                    val _listOperatingMode = responseGetOperatingMode.value as List<*>
                    val listOperatingMode = _listOperatingMode.map { it as OperatingModeModel }

                    val _listProductAvailabilityModel = responseGetProductAvailability.value as List<*>
                    val listProductAvailabilityModel = _listProductAvailabilityModel.map { it as ProductAvailabilityModel }

                    mapViewModel.fillData(
                        city = city,
                        listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails,
                        listOperatingMode = listOperatingMode,
                        listProductAvailabilityModel = listProductAvailabilityModel
                    )
                }
                TYPE_GET_CITY_BY_USER_ID + TYPE_GET_PHARMACY_ADDRESSES_DETAILS + TYPE_GET_OPERATING_MODE -> {
                    val resultGetCityByUserId = listRequests.find { it.type == TYPE_GET_CITY_BY_USER_ID }?.result!!.asSuccess()!!
                    val resultGetPharmacyAddressesDetails = listRequests.find { it.type == TYPE_GET_PHARMACY_ADDRESSES_DETAILS }?.result!!.asSuccess()!!
                    val resultGetOperatingMode = listRequests.find { it.type == TYPE_GET_OPERATING_MODE }?.result!!.asSuccess()!!

                    val responseGetCityByUserId = resultGetCityByUserId.data as ResponseValueModel<*>
                    val responseGetPharmacyAddressesDetails = resultGetPharmacyAddressesDetails.data as ResponseValueModel<*>
                    val responseGetOperatingMode = resultGetOperatingMode.data as ResponseValueModel<*>

                    val city = responseGetCityByUserId.value as String

                    val _listAllPharmacyAddressesDetails = responseGetPharmacyAddressesDetails.value as List<*>
                    val listAllPharmacyAddressesDetails = _listAllPharmacyAddressesDetails.map { it as PharmacyAddressesDetailsModel }

                    val _listOperatingMode = responseGetOperatingMode.value as List<*>
                    val listOperatingMode = _listOperatingMode.map { it as OperatingModeModel }

                    mapViewModel.fillData(
                        city = city,
                        listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails,
                        listOperatingMode = listOperatingMode
                    )
                }

            }

            binding.mapView.visibility = View.VISIBLE
            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    fun onErrorResultListener(exception: Exception) {
        binding.mapView.visibility = View.GONE
        val message = getErrorMessage(exception = exception)
        updateUI(FLAG_ERROR_RESULT, messageError = getString(message))
    }

    fun onLoadingResultListener() {
        binding.mapView.visibility = View.GONE
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    fun updateUI(flag: String, messageError: String? = null) = with(binding.layoutPendingResultMap) {
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

        mapViewModel.installCity(
            city = city,
            novocheboksarsk = getString(R.string.novocheboksarsk),
            cheboksary = getString(R.string.cheboksary)
        ){ zoom, geoPoint ->
            controller.setZoom(zoom)
            controller.setCenter(geoPoint)
        }

    }

    /**
     * Установка маркеров на карту в зависимости от флаг.
     *
     * Параметры:
     * [listAllPharmacyAddressesDetails] - список подробной информации о всех аптеках.
     */
    private fun installMarkers(listAllPharmacyAddressesDetails: List<PharmacyAddressesDetailsModel>) = with(binding) {

        val controller = mapView.controller

        mapViewModel.installMarkers(listAllPharmacyAddressesDetails = listAllPharmacyAddressesDetails){ flagStatus,drawableInt, geoPoint, pharmacyAddressesDetailsModel, totalNumber, availableQuantity ->
            try {
                val marker = Marker(mapView)

                // Установка координат для маркера
                marker.position = geoPoint

                val drawable = ContextCompat.getDrawable(
                    requireContext(),
                    drawableInt
                )

                marker.icon = drawable
                marker.title = pharmacyAddressesDetailsModel.pharmacyAddressesModel.address
                marker.setOnMarkerClickListener { marker, mapView ->

                    // Установка анимации при нажатии на маркер
                    controller.animateTo(geoPoint,18.0,1500L)

                    onClickMarker(
                        pharmacyAddressesDetailsModel = pharmacyAddressesDetailsModel,
                        flagStatus = flagStatus,
                        availableQuantity = availableQuantity,
                        totalNumber = totalNumber
                    )

                    true
                }

                // Добавление маркера на карту
                mapView.overlays.add(marker)
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
            }
        }
    }


    private fun onClickMarker(
        pharmacyAddressesDetailsModel: PharmacyAddressesDetailsModel,
        availableQuantity: Int?,
        totalNumber: Int?,
        flagStatus: Int
    ) = with(PharmacyDetailsBottomSheetDialogFragment){
        mapViewModel.onClickMarker(
            modeId = pharmacyAddressesDetailsModel.modeId
        ){ arrayListOperatingModesTimeFrom, arrayListOperatingModesTimeBefore, flag ->

            val pharmacyDetailsBottomSheetDialogFragment = newInstance(
                pharmacyAddressesDetailsModel = pharmacyAddressesDetailsModel,
                arrayListOperatingModesTimeFrom = arrayListOperatingModesTimeFrom,
                arrayListOperatingModesTimeBefore = arrayListOperatingModesTimeBefore,
                flagStatus = flagStatus,
                availableQuantity = availableQuantity,
                totalNumber = totalNumber,
                flag = flag
            )

            pharmacyDetailsBottomSheetDialogFragment.show(parentFragmentManager, TAG_PHARMACY_DETAILS_BOTTOM_SHEET)
        }
    }

    companion object {
        const val KEY_RESULT_FROM_PHARMACY_DETAILS = "KEY_RESULT_FROM_PHARMACY_DETAILS"
    }
}