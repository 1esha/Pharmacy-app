package com.example.pharmacyapp.tabs.home

import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.marginStart
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.databinding.FragmentHomeBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.home.adapters.AdvertisementAdapter
import com.example.pharmacyapp.tabs.home.viewmodels.HomeViewModel
import com.example.pharmacyapp.tabs.home.viewmodels.HomeViewModel.Companion.TYPE_GET_HOME_ADVERTISEMENT
import com.example.pharmacyapp.tabs.home.viewmodels.HomeViewModel.Companion.TYPE_GET_RECOMMENDED_PRODUCTS
import com.example.pharmacyapp.tabs.home.viewmodels.factories.HomeViewModelFactory
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class HomeFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var navControllerMain: NavController

    private val homeViewModel: HomeViewModel by viewModels(
        factoryProducer = { HomeViewModelFactory() }
    )

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        with(homeViewModel){
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
                    listProductModel.collect{ listProductModel ->
                        installRecommendedProducts(listProductModel = listProductModel)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    listAdvertisements.collect{ listAdvertisements ->
                        if (listAdvertisements != null){
                            installHomeAdvertisements(listAdvertisements = listAdvertisements)
                            initFillCounterAdvertisements{
                                setSelectedPosition(newPosition = 0)
                            }
                        }
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    selectedPosition.collect{ newPosition ->
                        fillCounterAdvertisements(newPosition = newPosition)
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        navControllerMain = getSupportActivity().getNavControllerMain()

        toolbarViewModel.installToolbar(
            toolbarSettingsModel = ToolbarSettingsModel(
                title = getString(
                    R.string.main
                )
            ){}
        )
        toolbarViewModel.clearMenu()

        layoutPendingResultAdvertisement.bTryAgain.setOnClickListener {
            homeViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeViewModel.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","HomeFragment onSuccessResultListener")
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
                TYPE_GET_HOME_ADVERTISEMENT + TYPE_GET_RECOMMENDED_PRODUCTS -> {
                    val resultGetHomeAdvertisement = listRequests.find { it.type == TYPE_GET_HOME_ADVERTISEMENT }?.result!!.asSuccess()!!
                    val resultGetRecommendedProducts = listRequests.find { it.type == TYPE_GET_RECOMMENDED_PRODUCTS }?.result!!.asSuccess()!!

                    val responseGetHomeAdvertisement = resultGetHomeAdvertisement.data as ResponseValueModel<*>
                    val responseGetRecommendedProducts = resultGetRecommendedProducts.data as ResponseValueModel<*>

                    val _listAdvertisements = responseGetHomeAdvertisement.value as List<*>
                    val listAdvertisements = _listAdvertisements.map { it as String }

                    val _listProductModel = responseGetRecommendedProducts.value as List<*>
                    val listProductModel = _listProductModel.map { it as ProductModel }

                    homeViewModel.fillData(
                        listProductModel = listProductModel,
                        listAdvertisements = listAdvertisements
                    )
                }
            }
            binding.tvWeRecommendIt.visibility = View.VISIBLE
            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        binding.tvWeRecommendIt.visibility = View.GONE
        val message = getErrorMessage(exception = exception)
        updateUI(FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        binding.tvWeRecommendIt.visibility = View.GONE
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultAdvertisement) {
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

    private fun installHomeAdvertisements(listAdvertisements: List<String>) = with(binding){
        vpAdvertisement.adapter = AdvertisementAdapter(
            listAdvertisement = listAdvertisements
        )

        vpAdvertisement.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                homeViewModel.setSelectedPosition(newPosition = position)
            }

        })
    }

    private fun fillCounterAdvertisements(newPosition: Int) = with(binding){
        homeViewModel.fillCounterAdvertisements(newPosition = newPosition) { selectedPosition, list ->
            layoutCounterAdvertisements.removeAllViews()
            for (i in list.indices){
                val view = layoutInflater.inflate(R.layout.item_couner,root,false)

                val image = view.findViewById<ImageView>(R.id.ivCircle)

                image.setImageResource(
                    if (i == selectedPosition) R.drawable.shape_circle_selected else R.drawable.shape_circle_not_selected
                )

                layoutCounterAdvertisements.addView(image)
            }
        }

    }

    private fun installRecommendedProducts(listProductModel: List<ProductModel>) = with(binding){
        homeViewModel.fillRecommendedProducts(listProductModel = listProductModel) { listChunkedProductModel ->
            listChunkedProductModel.forEach { _listProductModel ->

                val newHorizontalLayout = layoutInflater.inflate(R.layout.linear_layout_horizontal_for_recommended_products,root,false)

                homeViewModel.fillHorizontalLayoutRecommendedProducts(listProductModel = _listProductModel){ productModel,isDiscount,isFirst,textDiscount,textOriginalPrice,textPrice ->

                    val view = layoutInflater.inflate(R.layout.item_recommended_product,root,false)

                    val image = view.findViewById<ImageView>(R.id.ivProductRecommendedProduct)
                    val productName = view.findViewById<TextView>(R.id.tvProductNameRecommendedProduct)
                    val price = view.findViewById<TextView>(R.id.tvPriceRecommendedProduct)
                    val discount = view.findViewById<TextView>(R.id.tvDiscountRecommendedProduct)

                    val layoutOriginalPriceRecommendedProduct = view.findViewById<LinearLayout>(R.id.layoutOriginalPriceRecommendedProduct)
                    val originalPrice = view.findViewById<TextView>(R.id.tvOriginalPriceRecommendedProduct)

                    image.load(productModel.image)

                    productName.text = productModel.title

                    price.text = textPrice

                    originalPrice.text = textOriginalPrice
                    originalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

                    discount.text = textDiscount

                    if (isDiscount){
                        discount.visibility = View.VISIBLE
                        layoutOriginalPriceRecommendedProduct.visibility = View.VISIBLE
                    }
                    else{
                        discount.visibility = View.GONE
                        layoutOriginalPriceRecommendedProduct.visibility = View.GONE
                    }

                    val containerFirst = newHorizontalLayout.findViewById<FrameLayout>(R.id.containerFirst)
                    val containerSecond = newHorizontalLayout.findViewById<FrameLayout>(R.id.containerSecond)


                    if (isFirst){
                        containerFirst.addView(view)
                        containerFirst.setOnClickListener { navigateToProductInfo(productId = productModel.productId) }
                    }
                    else{
                        containerSecond.addView(view)
                        containerSecond.setOnClickListener { navigateToProductInfo(productId = productModel.productId) }
                    }
                }

                layoutRecommendedProducts.addView(newHorizontalLayout)

            }
        }
    }

    private fun navigateToProductInfo(productId: Int){
        findNavController().navigate(
            R.id.action_homeFragment_to_productInfoFragmentHome,
            bundleOf(KEY_PRODUCT_ID to productId)
        )
    }
}