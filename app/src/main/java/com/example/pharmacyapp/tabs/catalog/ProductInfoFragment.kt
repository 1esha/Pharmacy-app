package com.example.pharmacyapp.tabs.catalog

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.models.DetailsProductModel
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_FAVORITE_MODEL
import com.example.pharmacyapp.KEY_IS_FAVORITES
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_RESULT_IS_SHOWN_GET_ALL_FAVORITES
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID
import com.example.pharmacyapp.TYPE_GET_PRODUCT_BY_ID
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentProductInfoBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductInfoViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.ProductInfoViewModelFactory
import java.lang.Exception
import kotlin.math.roundToInt
import kotlin.properties.Delegates


class ProductInfoFragment : Fragment(), CatalogResult {

    private var _binding: FragmentProductInfoBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productInfoViewModel: ProductInfoViewModel by viewModels(
        factoryProducer = { ProductInfoViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerCatalog: NavController

    private var productId by Delegates.notNull<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navControllerCatalog = findNavController()

        productId = arguments?.getInt(KEY_PRODUCT_ID) ?:
        throw NullPointerException("ProductInfoFragment productId = null")

        val isShownGetProductById = productInfoViewModel.isShownGetProductById
        val isShownGetProductAvailability = productInfoViewModel.isShownGetProductAvailabilityByProductId

        if (!isShownGetProductById) {

            onSuccessfulEvent(type = TYPE_GET_PRODUCT_BY_ID) {
                productInfoViewModel.getProductById(productId = productId)
            }

        }

        if (!isShownGetProductAvailability) {

            onSuccessfulEvent(type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID) {
                productInfoViewModel.getProductAvailabilityByProductId(productId = productId)
            }

        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductInfoBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding){

        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                onBack {
                    navControllerCatalog.popBackStack()
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = EMPTY_STRING,
            icon = R.drawable.ic_back
        ) {
            onBack { navControllerCatalog.navigateUp() }
        })

        val isFavorite = arguments?.getBoolean(KEY_IS_FAVORITES) ?: false

        toolbarViewModel.inflateMenu(menu = if (isFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
        toolbarViewModel.setMenuClickListener { itemId ->
            onClickMenuItem(itemId)
        }

        productInfoViewModel.mediatorProductInfo.observe(viewLifecycleOwner) { mediatorResult ->
            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    toolbarViewModel.inflateMenu(menu = if (isFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
                    onSuccessResultListener(
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = productInfoViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

        productInfoViewModel.productModel.observe(viewLifecycleOwner) { productModel ->

            installBasicInfo(list = productModel.product_basic_info)

            val originalPrice = productModel.price
            val discount = productModel.discount
            val sumDiscount = ((discount / 100) * originalPrice)
            val price = originalPrice - sumDiscount
            val sumClubDiscount = ((CLUB_DISCOUNT / 100) * price)
            val priceClub = price - sumClubDiscount

            val textOriginalPrice = originalPrice.roundToInt().toString()
            val textDiscount = "-"+discount.roundToInt().toString()
            val textPrice = price.roundToInt().toString()
            val textPriceClub = priceClub.roundToInt().toString()

            ivProductInfo.load(productModel.image)

            tvProductNameInfo.text = productModel.title

            tvPriceWithClubCardInfo.text = textPriceClub

            tvPriceInfo.text = textPrice
            tvOriginalPriceInfo.text = textOriginalPrice
            tvOriginalPriceInfo.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            tvDiscountInfo.text = textDiscount

            if (discount == 0.0) {
                cardDiscount.visibility = View.GONE
                tvOriginalPriceInfo.visibility = View.GONE
                tvRubleSignForOriginalPrice.visibility = View.GONE
            }
            else {
                cardDiscount.visibility = View.VISIBLE
                tvOriginalPriceInfo.visibility = View.VISIBLE
                tvRubleSignForOriginalPrice.visibility = View.VISIBLE

            }
        }

        productInfoViewModel.listProductAvailability.observe(viewLifecycleOwner) { listProductAvailability ->

            // получаем список наличия товаров с количеством товаров в аптеках больше 0
            val listOnlyProductAvailability = listProductAvailability?.filter { productAvailabilityModel ->
                productAvailabilityModel.numberProducts > 0
            }

            // получаем список id аптек в которых количество товара больше 0
            val listPharmacy = listOnlyProductAvailability?.map { productAvailabilityModel ->
                return@map productAvailabilityModel.addressId
            }

            // получаем количевто аптек с количеством товара больше 0
            val numberPharmaciesWithProduct = listPharmacy?.size ?: 0

            // получаем сторку количества аптек в которых есть выбранный товар. В зависимости от количества аптек меняется текст строки
            val textNumberPharmaciesWithProduct = when(numberPharmaciesWithProduct) {
                0 -> {
                    getString(R.string.out_of_stock)
                }
                1 -> {
                    getString(R.string.available_in) +
                            " $numberPharmaciesWithProduct " +
                            getString(R.string.pharmacy)
                }
                else -> {
                    getString(R.string.available_in) +
                            " $numberPharmaciesWithProduct " +
                            getString(R.string.pharmacies)
                }
            }

            tvNumberPharmaciesWithProduct.text = textNumberPharmaciesWithProduct
            Log.i("TAG","listPharmacy = $listPharmacy")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(value: T, type: String?) {

        when(type) {
            TYPE_GET_PRODUCT_BY_ID -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_GET_PRODUCT_BY_ID")
                val isShownGetProductById = productInfoViewModel.isShownGetProductById

                if (!isShownGetProductById) {

                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {
                        val productModel = responseValueModel.value as ProductModel

                        productInfoViewModel.setProductModel(productModel = productModel)

                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        productInfoViewModel.setResultGetProductById(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }

                }

                productInfoViewModel.setIsShownGetProductById(isShown = true)

            }
            TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID")
                val isShownGetProductAvailability = productInfoViewModel.isShownGetProductAvailabilityByProductId
                Log.i("TAG","isShownGetProductAvailability = $isShownGetProductAvailability")
                if (!isShownGetProductAvailability) {

                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val list = responseValueModel.value as List<*>

                        val listProductAvailability = list.map {
                            return@map it as ProductAvailabilityModel
                        }

                        Log.i("TAG","listProductAvailability = $listProductAvailability")

                        productInfoViewModel.setListProductAvailability(listProductAvailability = listProductAvailability)

                        updateUI(flag = FLAG_SUCCESS_RESULT)
                    }
                    else {
                        productInfoViewModel.setResultGetProductAvailabilityByProductId(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }

                }

                productInfoViewModel.setIsShownGetProductAvailabilityByProductId(isShown = true)
            }
            TYPE_REMOVE_FAVORITES -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_REMOVE_FAVORITES")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    Log.i("TAG","ProductInfoFragment onSuccessResultListener Remove OK")

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productInfoViewModel.setResultRemoveFavorites(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_ADD_FAVORITE -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_ADD_FAVORITE")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    Log.i("TAG","ProductInfoFragment onSuccessResultListener Add OK")

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productInfoViewModel.setResultAddFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
        }
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        productInfoViewModel.setIsShownGetProductById(isShown = true)
        productInfoViewModel.setIsShownGetProductAvailabilityByProductId(isShown = true)
        toolbarViewModel.clearMenu()
        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
    }

    override fun onPendingResultListener() {
        productInfoViewModel.clearErrorType()
        toolbarViewModel.clearMenu()
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
                    TYPE_GET_PRODUCT_BY_ID -> productInfoViewModel.setResultGetProductById(result = PendingResult())
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID -> productInfoViewModel.setResultGetProductAvailabilityByProductId(result = PendingResult())
                    TYPE_REMOVE_FAVORITES -> productInfoViewModel.setResultRemoveFavorites(result = PendingResult())
                    TYPE_ADD_FAVORITE -> productInfoViewModel.setResultAddFavorite(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                when(type) {
                    TYPE_GET_PRODUCT_BY_ID -> productInfoViewModel.setResultGetProductById(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID -> productInfoViewModel.setResultGetProductAvailabilityByProductId(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_REMOVE_FAVORITES -> productInfoViewModel.setResultRemoveFavorites(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_ADD_FAVORITE -> productInfoViewModel.setResultAddFavorite(result = ErrorResult(exception = currentException), errorType = errorType)
                }
            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultProductInfo) {
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

    private fun onClickMenuItem(itemId: Int) {

        val sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES,Context.MODE_PRIVATE)
        val userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        if (userId == UNAUTHORIZED_USER) {
            val navCallbackMain = getSupportActivity().getNavControllerMain()
            navCallbackMain.navigate(R.id.nav_graph_log_in)
            return
        }

        when (itemId) {
            R.id.favorite -> {
                onSuccessfulEvent(type = TYPE_REMOVE_FAVORITES) {
                    productInfoViewModel.removeFavorite(productId = productId)
                    toolbarViewModel.inflateMenu(menu = R.menu.menu_favorite_border)
                    arguments?.putBoolean(KEY_IS_FAVORITES, false)
                }
            }
            R.id.favorite_border -> {
                onSuccessfulEvent(type = TYPE_ADD_FAVORITE) {
                    val productModel = productInfoViewModel.productModel.value ?:
                    throw NullPointerException("ProductInfoFragment productModel = null")
                    productInfoViewModel.addFavorite(favoriteModel = FavoriteModel(
                        productId = productModel.product_id,
                        title = productModel.title,
                        productPath = productModel.product_path,
                        price = productModel.price,
                        discount = productModel.discount,
                        image = productModel.image
                    ))
                    toolbarViewModel.inflateMenu(menu = R.menu.menu_favorite)
                    arguments?.putBoolean(KEY_IS_FAVORITES, true)
                }

            }
        }
    }

    private fun getFavoriteModel(): FavoriteModel {

        val productModel = productInfoViewModel.productModel.value ?:
        throw NullPointerException("ProductInfoFragment productModel = null")

        val favoriteModel = FavoriteModel(
            productId = productModel.product_id,
            title = productModel.title,
            productPath = productModel.product_path,
            price = productModel.price,
            discount = productModel.discount,
            image = productModel.image
        )

        return favoriteModel
    }

    // проверка на наличие ошибки
    // возвращаем true если имеется ошибка иначе false
    private fun errorChecking(): Boolean {
        val mediatorResult = productInfoViewModel.mediatorProductInfo.value as MediatorResultsModel<*>
        val result = mediatorResult.result as Result<*>

        return if (result is SuccessResult) false else true
    }

    // обработка безопасного возвращения на экран ProductFragment
    private fun onBack(listener: () -> Unit) {
        if (errorChecking()) {
            listener()
            return
        }
        val isFavorite = arguments?.getBoolean(KEY_IS_FAVORITES) ?: false
        val result = Bundle()
        result.putSerializable(KEY_FAVORITE_MODEL, getFavoriteModel())
        result.putBoolean(KEY_IS_FAVORITES, isFavorite)
        getSupportActivity().setFragmentResult(requestKey = KEY_RESULT_IS_SHOWN_GET_ALL_FAVORITES, result = result)

        listener()
    }

    // добавление в layoutBasicInfo основную инвормацию о товаре
    private fun installBasicInfo(list:List<Map<String,String>>) = with(binding){

        val mutableListDetailsProduct = mutableListOf<DetailsProductModel>()

        list.forEach { map ->
            map.forEach { key, value ->
                val detailsProductModel = DetailsProductModel(
                    title = key,
                    body = value
                )
                mutableListDetailsProduct.add(detailsProductModel)
            }
        }

        val numberLines = mutableListDetailsProduct.size - 1

        for (index in 0..numberLines) {

            val detailsProductModel = mutableListDetailsProduct[index]

            // создание LinearLayout с горизонтальной ориентацией, который будет хранить два TextView -
            // заголовок основной информации и содержание основной информации
            val newHorizontalLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (layoutParams as LinearLayout.LayoutParams).setMargins(0, 32, 0, 8)

            }

            // создание разделителя, который будет находиться между элементами информации
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    3
                )
                val colorDivider = resources.getColor(R.color.gray300, resources.newTheme())

                setBackgroundColor(colorDivider)
            }

            // создание TextView для заголовка
            val newTextViewTitle = TextView(requireContext()).apply {
                text = detailsProductModel.title
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setTextAppearance(android.R.style.TextAppearance_Material_Body2)
            }

            // создание TextView для содержимого
            val newTextViewBody = TextView(requireContext()).apply {
                text = detailsProductModel.body

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            }

            // добавление созданных view в layoutBasicInfo
            newHorizontalLayout.addView(newTextViewTitle)
            newHorizontalLayout.addView(newTextViewBody)
            layoutBasicInfo.addView(newHorizontalLayout)
            // проверка на последний элемент
            // последний элемент не должен иметь разделитель
            if (index < numberLines) layoutBasicInfo.addView(divider)
        }
    }

}