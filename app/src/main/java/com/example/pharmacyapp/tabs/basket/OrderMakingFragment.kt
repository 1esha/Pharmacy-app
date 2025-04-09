package com.example.pharmacyapp.tabs.basket

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.basket.models.BasketModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_CREATE_ORDER
import com.example.pharmacyapp.TYPE_DELETE_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID
import com.example.pharmacyapp.TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET
import com.example.pharmacyapp.TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentOrderMakingBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.basket.viewmodels.OrderMakingViewModel
import com.example.pharmacyapp.tabs.basket.viewmodels.factories.OrderMakingViewModelFactory
import kotlinx.coroutines.launch

class OrderMakingFragment: Fragment(), ResultProcessing {

    private var _binding: FragmentOrderMakingBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val orderMakingViewModel: OrderMakingViewModel by viewModels(
        factoryProducer = { OrderMakingViewModelFactory() }
    )

    private lateinit var navControllerBasket: NavController

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        orderMakingViewModel.initValues(
            userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER),
            addressId = arguments?.getInt(KEY_CURRENT_ADDRESS_ID),
            city = arguments?.getString(KEY_CURRENT_CITY),
            address = arguments?.getString(KEY_CURRENT_ADDRESS),
            arrayListNumberProducts = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_ORDER_MAKING),
            arrayListIdsProducts = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_ORDER_MAKING),
        )

        orderMakingViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                orderMakingViewModel.stateScreen.collect{ result ->
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
                orderMakingViewModel.listBasketModel.collect{ listBasketModel ->
                    fillingLayoutProductsForOrder(listBasketModel = listBasketModel)
                    fillingTotalAmount()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                orderMakingViewModel.listAvailableQuantity.collect{ listAvailableQuantity ->
                    installLayoutAvailability(listAvailableQuantity = listAvailableQuantity)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                orderMakingViewModel.address.collect{ address ->
                    binding.tvAddressCurrentPharmacy.text = address
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                orderMakingViewModel.city.collect{ city ->
                    binding.tvCityCurrentPharmacy.text = city
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderMakingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerBasket = findNavController()

        navControllerMain = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.making_an_order),
            icon = R.drawable.ic_back,
            subTitle = getString(R.string.step_of, 2, 2)
        ){
            navControllerBasket.navigateUp()
        })
        toolbarViewModel.clearMenu()

        layoutPendingResultCurrentOrderMaking.bTryAgain.setOnClickListener {
            orderMakingViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        bPlaceAnOrder.setOnClickListener {
            orderMakingViewModel.onClickPlaceAnOrder(isNetworkStatus = isNetworkStatus)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        orderMakingViewModel.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","OrderMakingFragment onSuccessResultListener")
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
                TYPE_GET_PRODUCTS_FROM_BASKET + TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID -> {
                    val resultGetProductsFromBasket = listRequests.find { it.type == TYPE_GET_PRODUCTS_FROM_BASKET }?.result!!.asSuccess()!!
                    val resultGetProductAvailabilityByAddressId = listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID }?.result!!.asSuccess()!!

                    val responseGetProductsFromBasket  = resultGetProductsFromBasket.data as ResponseValueModel<*>
                    val responseGetProductAvailabilityByAddressId = resultGetProductAvailabilityByAddressId.data as ResponseValueModel<*>

                    val _listBasketModel = responseGetProductsFromBasket.value as List<*>
                    val listBasketMode = _listBasketModel.map { it as BasketModel }

                    val _listProductAvailabilityModel = responseGetProductAvailabilityByAddressId.value as List<*>
                    val listProductAvailabilityModel = _listProductAvailabilityModel.map { it as ProductAvailabilityModel }

                    listBasketMode.forEach {
                        Log.d("TAG","item Basket: productId = ${it.productModel.productId},numberProducts = ${it.numberProducts}")
                    }

                    listProductAvailabilityModel.forEach {
                        Log.d("TAG","item Availability: addressId = ${it.addressId},productId = ${it.productId},numberProducts = ${it.numberProducts}")
                    }

                    orderMakingViewModel.fillData(
                        listBasketMode = listBasketMode,
                        listProductAvailabilityModel = listProductAvailabilityModel
                    )
                }
                TYPE_DELETE_PRODUCTS_FROM_BASKET + TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET + TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY + TYPE_CREATE_ORDER -> {
                    navigateToReadyOrder()
                }
                TYPE_DELETE_PRODUCTS_FROM_BASKET + TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY + TYPE_CREATE_ORDER -> {
                    navigateToReadyOrder()
                }
                TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET + TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY + TYPE_CREATE_ORDER -> {
                    navigateToReadyOrder()
                }

            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: java.lang.Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        val message = getErrorMessage(exception = exception)
        updateUI(FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultCurrentOrderMaking){
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

    private fun fillingLayoutProductsForOrder(listBasketModel: List<BasketModel>) = with(binding){
        orderMakingViewModel.fillingLayoutProductsForOrder(listBasketModel = listBasketModel) { basketModel, isVisibleDivider ->
            val view = layoutInflater.inflate(R.layout.item_order_making,root,false)

            val ivProductOrderMaking = view.findViewById<ImageView>(R.id.ivProductOrderMaking)
            val tvProductNameOrderMaking = view.findViewById<TextView>(R.id.tvProductNameOrderMaking)
            val tvNumberPieces = view.findViewById<TextView>(R.id.tvNumberPieces)
            val divider = view.findViewById<View>(R.id.dividerOrderMaking)

            ivProductOrderMaking.load(basketModel.productModel.image)
            tvProductNameOrderMaking.text = basketModel.productModel.title

            val textNumberPieces = basketModel.numberProducts.toString()
            tvNumberPieces.text = textNumberPieces

            divider.visibility = if (isVisibleDivider) View.VISIBLE else View.GONE

            layoutProductsForOrder.addView(view)
        }
    }

    private fun fillingTotalAmount() = with(binding){
        orderMakingViewModel.fillingTotalAmount { textNumberPiecesTotal,textOrderAmount,textDiscountTotal,textClubDiscountTotal,textTotalPrice,isVisibleDiscount ->
            tvNumberPiecesTotal.text = textNumberPiecesTotal
            tvOrderAmount.text = textOrderAmount
            tvDiscountTotal.text = textDiscountTotal
            tvClubDiscountTotal.text = textClubDiscountTotal
            tvTotalPrice.text = textTotalPrice

            layoutTotalDiscount.visibility = if (isVisibleDiscount) View.VISIBLE else View.GONE
        }
    }

    private fun installLayoutAvailability(listAvailableQuantity: List<Int>) = with(binding){
        orderMakingViewModel.installLayoutAvailability(listAvailableQuantity = listAvailableQuantity) { availableQuantity, totalNumber, isVisible ->
            layoutAvailabilityInCurrentPharmacy.visibility = if (isVisible) View.VISIBLE else View.GONE

            tvNumberAvailableProducts.text = getString(R.string.out_of_products_are_available, availableQuantity, totalNumber)
        }
    }

    private fun navigateToReadyOrder(){

    }

    companion object {
        const val KEY_CURRENT_ADDRESS_ID = "KEY_CURRENT_ADDRESS_ID"
        const val KEY_CURRENT_CITY = "KEY_CURRENT_CITY"
        const val KEY_CURRENT_ADDRESS = "KEY_CURRENT_ADDRESS"
        const val KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_ORDER_MAKING = "KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_ORDER_MAKING"
        const val KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_ORDER_MAKING = "KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_ORDER_MAKING"
    }
}