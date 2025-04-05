package com.example.pharmacyapp.tabs.basket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.models.AvailabilityInPharmacyModel
import com.example.domain.models.AvailabilityProductsForOrderMakingModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.ColorUtils
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
import com.example.pharmacyapp.TYPE_GET_PHARMACY_ADDRESSES
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentChooseAddressForOrderMakingBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.basket.adapters.ChooseAddressForOrderMakingAdapter
import com.example.pharmacyapp.tabs.basket.viewmodels.ChooseAddressForOrderMakingViewModel
import com.example.pharmacyapp.tabs.basket.viewmodels.factories.ChooseAddressForOrderMakingViewModelFactory
import kotlinx.coroutines.launch

class ChooseAddressForOrderMakingFragment() : Fragment(), ResultProcessing {

    private var _binding: FragmentChooseAddressForOrderMakingBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val chooseAddressForOrderMakingViewModel: ChooseAddressForOrderMakingViewModel by viewModels(
        factoryProducer = { ChooseAddressForOrderMakingViewModelFactory() }
    )

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var chooseAddressForOrderMakingAdapter: ChooseAddressForOrderMakingAdapter

    private lateinit var navControllerBasket: NavController

    private lateinit var navControllerMain: NavController

    private val isNetworkStatus get() = getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        chooseAddressForOrderMakingViewModel.initValues(
            userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER),
            arrayListIdsSelectedBasketModels = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP),
            arrayListNumberProductsSelectedBasketModels = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP)
        )

        chooseAddressForOrderMakingViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chooseAddressForOrderMakingViewModel.stateScreen.collect{ state ->
                    when(state){
                        is Result.Loading -> {
                            onLoadingResultListener()
                        }
                        is Result.Success<*> -> {
                            onSuccessResultListener(data = state.data)
                        }
                        is Result.Error -> {
                            onErrorResultListener(exception = state.exception)
                        }

                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                chooseAddressForOrderMakingViewModel.listAvailabilityProductsForOrderMakingModel.collect{ listAvailabilityProductsForOrderMakingModel ->
                    installUI(listAvailabilityProductsForOrderMakingModel = listAvailabilityProductsForOrderMakingModel)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseAddressForOrderMakingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerBasket = findNavController()

        navControllerMain = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.making_an_order),
            icon = R.drawable.ic_back
        ){
            navControllerBasket.navigateUp()
        })
        toolbarViewModel.clearMenu()

        bOnTheMap.setOnClickListener {
            chooseAddressForOrderMakingViewModel.navigateOnMap { flag, arrayListIdsSelectedBasketModels, arrayListNumberProductsSelectedBasketModels ->
                val bundle = Bundle().apply {
                    putString(KEY_FLAGS_FOR_MAP,flag)
                    putIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP,arrayListIdsSelectedBasketModels)
                    putIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP,arrayListNumberProductsSelectedBasketModels)
                }

                navControllerMain.navigate(R.id.mapFragment,bundle)
            }
        }

        layoutPendingResultOrderMaking.bTryAgain.setOnClickListener {
            chooseAddressForOrderMakingViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        try {
            Log.i("TAG","onSuccessResultListener")
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
                TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS + TYPE_GET_PHARMACY_ADDRESSES + TYPE_GET_CITY_BY_USER_ID -> {
                    Log.i("TAG","fullType = TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS + TYPE_GET_PHARMACY_ADDRESSES + TYPE_GET_CITY_BY_USER_ID")

                    val resultGetProductAvailability = listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS }?.result!!.asSuccess()!!
                    val resultGetPharmacyAddresses = listRequests.find { it.type == TYPE_GET_PHARMACY_ADDRESSES }?.result!!.asSuccess()!!
                    val resultGetCityByUserId = listRequests.find { it.type == TYPE_GET_CITY_BY_USER_ID }?.result!!.asSuccess()!!

                    val responseGetProductAvailability = resultGetProductAvailability.data as ResponseValueModel<*>
                    val responseGetPharmacyAddresses = resultGetPharmacyAddresses.data as ResponseValueModel<*>
                    val responseGetCityByUserId = resultGetCityByUserId.data as ResponseValueModel<*>

                    val _listProductAvailabilityModel = responseGetProductAvailability.value as List<*>
                    val listProductAvailabilityModel = _listProductAvailabilityModel.map { it as ProductAvailabilityModel }

                    val _listPharmacyAddressesModel = responseGetPharmacyAddresses.value as List<*>
                    val listPharmacyAddressesModel = _listPharmacyAddressesModel.map { it as PharmacyAddressesModel }

                    val city = responseGetCityByUserId.value as String

                    listProductAvailabilityModel.forEach {
                        Log.d("TAG","it = $it")
                    }

                    chooseAddressForOrderMakingViewModel.fillData(
                        listProductAvailabilityModel = listProductAvailabilityModel,
                        listPharmacyAddressesModel = listPharmacyAddressesModel,
                        city = city
                    )
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultOrderMaking){
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

    private fun installUI(listAvailabilityProductsForOrderMakingModel: List<AvailabilityProductsForOrderMakingModel>) = with(binding){
        chooseAddressForOrderMakingViewModel.installAdapter { totalNumber ->
            val colorUtils = ColorUtils(context = requireContext())
            val availabilityInPharmacyModel = AvailabilityInPharmacyModel(
                colorInStock = colorUtils.getColor(color = R.color.green800),
                colorOutOfStock = colorUtils.getColor(color = R.color.red700),
                colorWarning = colorUtils.getColor(color = R.color.warning),
                textInStock = getString(R.string.all_products_are_in_stock),
                textOutOfStock = getString(R.string.out_of_stock),
                textWarning = getString(R.string.available_out_of)
            )
            chooseAddressForOrderMakingAdapter = ChooseAddressForOrderMakingAdapter(
                listAvailabilityProductsForOrderMakingModel = listAvailabilityProductsForOrderMakingModel,
                totalNumber = totalNumber,
                availabilityInPharmacyModel = availabilityInPharmacyModel,
                onClick = ::chooseAddress
            )

            rvOrderMaking.adapter = chooseAddressForOrderMakingAdapter
            rvOrderMaking.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    fun chooseAddress(addressId: Int){
        chooseAddressForOrderMakingViewModel.chooseAddress { arrayListIdsSelectedBasketModels, arrayListNumberProductsSelectedBasketModels ->

        }
    }

}