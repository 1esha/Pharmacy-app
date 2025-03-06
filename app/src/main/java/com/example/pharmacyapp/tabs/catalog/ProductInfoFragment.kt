package com.example.pharmacyapp.tabs.catalog

import android.content.Context
import android.content.SharedPreferences
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
import com.example.pharmacyapp.ColorUtils
import com.example.pharmacyapp.FLAG_CURRENT_PRODUCT
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_BODY_INSTRUCTION
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_AVAILABILITY_PHARMACY_ADDRESSES_DETAILS
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.KEY_ARRAY_LIST_TITLES_INSTRUCTION
import com.example.pharmacyapp.KEY_FAVORITE_MODEL
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.KEY_IS_FAVORITES
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_RESULT_FROM_PRODUCT_INFO
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
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
import com.example.pharmacyapp.toArrayListInt
import java.lang.Exception
import kotlin.math.roundToInt
import kotlin.properties.Delegates


/**
 * Класс [ProductInfoFragment] отвечает за отрисовку и работу экрана подробной информации о товаре.
 */
class ProductInfoFragment : Fragment(), CatalogResult {

    private var _binding: FragmentProductInfoBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productInfoViewModel: ProductInfoViewModel by viewModels(
        factoryProducer = { ProductInfoViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerCatalog: NavController

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private var productId by Delegates.notNull<Int>()

    private var userId by Delegates.notNull<Int>()

    private val arrayListTitles = arrayListOf<String>()
    private val arrayListBody = arrayListOf<String>()

    private var image: String by Delegates.notNull()

    private var arrayListIdsAvailabilityPharmacyAddresses: ArrayList<Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES,Context.MODE_PRIVATE)

        userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        navControllerCatalog = findNavController()

        productId = arguments?.getInt(KEY_PRODUCT_ID) ?:
        throw NullPointerException("ProductInfoFragment productId = null")

        sendingRequests()

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

        navControllerMain = getSupportActivity().getNavControllerMain()

        // Создание callback отвечающий за обработку системной кнопки "Назад"
        val callback = object : OnBackPressedCallback(true) {

            override fun handleOnBackPressed() {
                onBack {
                    navControllerCatalog.popBackStack()
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,callback)

        // Установка toolbar
        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            icon = R.drawable.ic_back
        ) {
            onBack { navControllerCatalog.navigateUp() }
        })

        val isFavorite = arguments?.getBoolean(KEY_IS_FAVORITES) ?: false
        // Установка меню
        toolbarViewModel.inflateMenu(menu = if (isFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
        toolbarViewModel.setMenuClickListener { itemId ->
            onClickMenuItem(itemId)
        }

        // Обработка нажатия на картику товара
        ivProductInfo.setOnClickListener {
            // Установка картинки товара для передачи на экран с полным изображение
            val bundle = Bundle().apply {
                putString(FullImageProductFragment.KEY_FULL_IMAGE_PRODUCT,image)
            }
            // Открытие экрана с полным изображением товара
            navControllerMain.navigate(R.id.fullImageProductFragment, bundle)
        }

        // Обработка нажатия кнопки "В корзину"
        bInBasket.setOnClickListener {
            // Получение списка идентификаторов всех товаров из корзины
            val listIdsProductsFromBasket = productInfoViewModel.listIdsProductsFromBasket.value
            if (listIdsProductsFromBasket != null) {

                // Если товар есть в корзине
                val isInBasket = listIdsProductsFromBasket.any { it == productId }

                onClickInBasket(isInBasket = isInBasket)
            }

        }

        // Обработка нажатия на поле "Наличие"
        cardAvailability.setOnClickListener {

            /*
             Если на момент нажатия список идентификаторов аптек с наличием товара не пустной
             т.е результат запроса уже получен.
             */
            if (arrayListIdsAvailabilityPharmacyAddresses != null) {

                val bundle = Bundle().apply {
                    putIntegerArrayList(
                        KEY_ARRAY_LIST_IDS_AVAILABILITY_PHARMACY_ADDRESSES_DETAILS,
                        arrayListIdsAvailabilityPharmacyAddresses
                    )
                }

                // Передача флага для отрисовки разметки карты
                bundle.putString(KEY_FLAGS_FOR_MAP, FLAG_CURRENT_PRODUCT)

                // Открытие экрана карты с аптеками
                navControllerMain.navigate(R.id.mapFragment, bundle)
            }
            else {
                getSupportActivity().showToast(getString(R.string.try_again_later))
            }

        }
        // Обработка нажатия кнопки "Прпробовать снова"
        layoutPendingResultProductInfo.bTryAgain.setOnClickListener {

            productInfoViewModel.setIsShownGetProductById(isShown = false)
            productInfoViewModel.setIsShownGetProductAvailabilityByProductId(isShown = false)
            productInfoViewModel.setIsShownGetIdsProductsFromBasket(isShown = false)

            // Повторный вызов всех запросов
            sendingRequests()
        }

        // Обработка нажатия на поле "Инструкция по применению"
        cardInstruction.setOnClickListener {
            // Открытие экрана с инструкцие по применению
            val bundle = Bundle()
            bundle.apply {
                putStringArrayList(KEY_ARRAY_LIST_TITLES_INSTRUCTION, arrayListTitles)
                putStringArrayList(KEY_ARRAY_LIST_BODY_INSTRUCTION, arrayListBody)
            }

            navControllerCatalog.navigate(R.id.action_productInfoFragment_to_instructionManualFragment, bundle)
        }

        // Наблюдение за изменением значений - listProductAvailability, listIdsProductsFromBasket, productModel
        productInfoViewModel.mediatorIsAllRequests.observe(viewLifecycleOwner) {
            val listProductAvailability = productInfoViewModel.listProductAvailability.value
            val listIdsProductsFromBasket = productInfoViewModel.listIdsProductsFromBasket.value
            val productModel = productInfoViewModel.productModel.value

            // Если результаты по всем запросам пришли
            if (
                listProductAvailability != null &&
                listIdsProductsFromBasket != null &&
                productModel != null
            ) {
                // Установка информации о товаре
                installProductModel(productModel = productModel)

                // Установка наличия товаров в аптеках
                installProductAvailability(listProductAvailability = listProductAvailability)

                // Установка кнопки в зависимости от того находится ли товар в корзине или нет
                installButtonInBasket(listIdsProductsFromBasket)

                updateUI(flag = FLAG_SUCCESS_RESULT)

            }
        }

        // Наблюдение за получением результатов запросов
        productInfoViewModel.mediatorProductInfo.observe(viewLifecycleOwner) { mediatorResult ->

            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    val currentIsFavorite = arguments?.getBoolean(KEY_IS_FAVORITES) ?: false
                    toolbarViewModel.inflateMenu(menu = if (currentIsFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("TAG","ProductInfoFragment onDestroyView")
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

                if (!isShownGetProductAvailability) {

                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val _listProductAvailability = responseValueModel.value as List<*>
                        val listProductAvailability = _listProductAvailability.map {
                            return@map it as ProductAvailabilityModel
                        }

                        productInfoViewModel.setListProductAvailability(listProductAvailability = listProductAvailability)

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

                    toolbarViewModel.inflateMenu(menu = R.menu.menu_favorite_border)
                    arguments?.putBoolean(KEY_IS_FAVORITES, false)

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

                    toolbarViewModel.inflateMenu(menu = R.menu.menu_favorite)
                    arguments?.putBoolean(KEY_IS_FAVORITES, true)

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productInfoViewModel.setResultAddFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_GET_IDS_PRODUCTS_FROM_BASKET")
                val isShownGetIdsProductsFromBasket = productInfoViewModel.isShownGetIdsProductsFromBasket

                if (!isShownGetIdsProductsFromBasket) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {
                        val _listIdsProductsFromBasket = responseValueModel.value as List<*>
                        val listIdsProductsFromBasket = _listIdsProductsFromBasket.map {
                            return@map it as Int
                        }

                        productInfoViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = listIdsProductsFromBasket)
                    }
                    else {
                        productInfoViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                productInfoViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
            }
            TYPE_ADD_PRODUCT_IN_BASKET -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_ADD_PRODUCT_IN_BASKET")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    val listIdsProductsFromBasket = productInfoViewModel.listIdsProductsFromBasket.value?:
                    throw NullPointerException("ProductInfoFragment listIdsProductsFromBasket = null")

                    val mutableListIdsProductsFromBasket = listIdsProductsFromBasket.toMutableList()

                    mutableListIdsProductsFromBasket.add(productId)

                    productInfoViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = mutableListIdsProductsFromBasket)

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productInfoViewModel.setResultAddProductInBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_DELETE_PRODUCT_FROM_BASKET -> {
                Log.i("TAG","ProductInfoFragment onSuccessResultListener TYPE_DELETE_PRODUCT_FROM_BASKET")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    val listIdsProductsFromBasket = productInfoViewModel.listIdsProductsFromBasket.value?:
                    throw NullPointerException("ProductInfoFragment listIdsProductsFromBasket = null")

                    val mutableListIdsProductsFromBasket = listIdsProductsFromBasket.toMutableList()

                    mutableListIdsProductsFromBasket.remove(productId)

                    productInfoViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = mutableListIdsProductsFromBasket)

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productInfoViewModel.setResultDeleteProductFromBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
        }
    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        // Установка значения "не показано" для всех запросов
        productInfoViewModel.setIsShownGetProductById(isShown = true)
        productInfoViewModel.setIsShownGetProductAvailabilityByProductId(isShown = true)
        productInfoViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
        // Отчистка меню
        toolbarViewModel.clearMenu()

        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
    }

    override fun onPendingResultListener() {
        // Отчистка типы ошибки
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
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> productInfoViewModel.setResultGetIdsProductsFromBasket(result = PendingResult())
                    TYPE_ADD_PRODUCT_IN_BASKET -> productInfoViewModel.setResultAddProductInBasket(result = PendingResult())
                    TYPE_DELETE_PRODUCT_FROM_BASKET -> productInfoViewModel.setResultDeleteProductFromBasket(result = PendingResult())
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
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> productInfoViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_ADD_PRODUCT_IN_BASKET -> productInfoViewModel.setResultAddProductInBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_DELETE_PRODUCT_FROM_BASKET -> productInfoViewModel.setResultDeleteProductFromBasket(result = ErrorResult(exception = currentException), errorType = errorType)
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

    /**
     * Отправка всех, необходимых для работы экрана, запросов.
     */
    private fun sendingRequests() {
        val isShownGetProductById = productInfoViewModel.isShownGetProductById
        val isShownGetProductAvailability = productInfoViewModel.isShownGetProductAvailabilityByProductId
        val isShownGetIdsProductsFromBasket = productInfoViewModel.isShownGetIdsProductsFromBasket

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

        if(!isShownGetIdsProductsFromBasket) {
            onSuccessfulEvent(type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET) {
                productInfoViewModel.getIdsProductsFromBasket(userId = userId)
            }
        }
    }

    /**
     * Установка информации о товаре.
     *
     * Парметры:
     * [productModel] - данные текущего товара.
     */
    private fun installProductModel(productModel: ProductModel) = with(binding) {

        // Заполнените блока базовой информацие о товаре
        installBasicInfo(list = productModel.productBasicInfo)

        // Заполнение инструкции по применению данными
        fillingInstructions(list = productModel.productDetailedInfo)

        // Установка интерфейса
        image = productModel.image

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

    /**
     * Установка наличия товара в аптеках.
     *
     * Параметры:
     * [listProductAvailability] - список наличия товара в аптеках.
     */
    private fun installProductAvailability(listProductAvailability: List<ProductAvailabilityModel>) = with(binding) {

        // Получаем список наличия товаров с количеством товаров в аптеках больше 0
        val listOnlyProductAvailability = listProductAvailability.filter { productAvailabilityModel ->
            productAvailabilityModel.numberProducts > 0
        }

        // Получаем список id аптек в которых количество товара больше 0
        val listPharmacy = listOnlyProductAvailability.map { productAvailabilityModel ->
            return@map productAvailabilityModel.addressId
        } ?: emptyList()

        // Установка списка идентификаторов аптек с наличием товара для передачи на экран MapFragment
        arrayListIdsAvailabilityPharmacyAddresses = listPharmacy.toArrayListInt()

        // Получаем количевто аптек с количеством товара больше 0
        val numberPharmaciesWithProduct = listPharmacy.size

        // Получаем строку количества аптек в которых есть выбранный товар.
        // В зависимости от количества аптек меняется текст строки
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

    }

    /**
     * Установка внешнего вида кнопки "В корзину".
     *
     * Параметры:
     * [listIdsProductsFromBasket] - список идентификаторов товаров из корзины.
     */
    private fun installButtonInBasket(listIdsProductsFromBasket: List<Int>) = with(binding){

        // Значение находится ли товар в корзине или нет
        val isInBasket = listIdsProductsFromBasket.any { it == productId }

        val colorUtils = ColorUtils(context = requireContext())

        val colorPrimary = colorUtils.colorPrimary
        val colorSecondaryContainer = colorUtils.colorSecondaryContainer
        val colorOnSecondaryContainer = colorUtils.colorOnSecondaryContainer
        val colorOnPrimary = colorUtils.colorOnPrimary

        if (isInBasket) {
            bInBasket.text = getString(R.string.in_the_shopping_cart)
            bInBasket.setBackgroundColor(colorSecondaryContainer)
            bInBasket.setTextColor(colorOnSecondaryContainer)
        }
        else {
            bInBasket.text = getString(R.string.add_to_the_shopping_cart)
            bInBasket.setBackgroundColor(colorPrimary)
            bInBasket.setTextColor(colorOnPrimary)
        }

    }

    /**
     * Обработка нажатия на кнопку "В корзину".
     * Добавление/удаление товара из корзины.
     *
     * Параметры:
     * [isInBasket] - значение того находится товар в корзине или нет.
     */
    private fun onClickInBasket(isInBasket: Boolean) {

        // Если пользователь не авторизован, то открывается экране авторизации
        if (userId == UNAUTHORIZED_USER) {

            navControllerMain.navigate(R.id.nav_graph_log_in)
            return
        }

        // Добавление/удаление текущего товара из корзины
        if (isInBasket) {
            onSuccessfulEvent(type = TYPE_DELETE_PRODUCT_FROM_BASKET) {
                productInfoViewModel.deleteProductFromBasket(
                    userId = userId,
                    productId = productId
                )
            }
        }
        else {
            onSuccessfulEvent(type = TYPE_ADD_PRODUCT_IN_BASKET){
                productInfoViewModel.addProductInBasket(
                    userId = userId,
                    productId = productId
                )
            }

        }
    }

    /**
     * Обработка нажатия на кнопку на меню.
     * Добавление и удаление из "Избранного".
     *
     * Параметры:
     * [itemId] - идентификатор, нажатого элемента.
     */
    private fun onClickMenuItem(itemId: Int) {

        // Если пользователь не авторизован, то открывается экране авторизации
        if (userId == UNAUTHORIZED_USER) {

            navControllerMain.navigate(R.id.nav_graph_log_in)
            return
        }

        // Добавление и удаление из "Избранного"
        when (itemId) {
            // Удаление
            R.id.favorite -> {
                onSuccessfulEvent(type = TYPE_REMOVE_FAVORITES) {
                    productInfoViewModel.removeFavorite(productId = productId)

                }
            }
            // Добавление
            R.id.favorite_border -> {
                onSuccessfulEvent(type = TYPE_ADD_FAVORITE) {
                    val productModel = productInfoViewModel.productModel.value ?:
                    throw NullPointerException("ProductInfoFragment productModel = null")

                    productInfoViewModel.addFavorite(favoriteModel = FavoriteModel(
                        productId = productModel.productId,
                        title = productModel.title,
                        productPath = productModel.productPath,
                        price = productModel.price,
                        discount = productModel.discount,
                        image = productModel.image
                    ))

                }

            }
        }
    }

    /**
     * Получение FavoriteModel текущего товара.
     */
    private fun getFavoriteModel(): FavoriteModel {

        val productModel = productInfoViewModel.productModel.value ?:
        throw NullPointerException("ProductInfoFragment productModel = null")

        val favoriteModel = FavoriteModel(
            productId = productModel.productId,
            title = productModel.title,
            productPath = productModel.productPath,
            price = productModel.price,
            discount = productModel.discount,
            image = productModel.image
        )

        return favoriteModel
    }

    /**
     * Проверка на наличие ошибки.
     * Возвращает true если имеется ошибка иначе false.
     */
    private fun errorChecking(): Boolean {
        val mediatorResult = productInfoViewModel.mediatorProductInfo.value as MediatorResultsModel<*>
        val result = mediatorResult.result as Result<*>

        return if (result is SuccessResult) false else true
    }

    /**
     * Обработка безопасного возвращения на экран ProductFragment.
     *
     * Параметры:
     * [listener] - слушатель отвечающий за возращение на экран ProductFragment.
     */
    private fun onBack(listener: () -> Unit) {
        // Если ошибка, то просто возвращаемся
        if (errorChecking()) {
            listener()
            return
        }

        // Если ошибки нет,то передаем на экарн ProductFragment значения: isFavorite, favoriteModel, listIdsProductsFromBasket
        val isFavorite = arguments?.getBoolean(KEY_IS_FAVORITES) ?: false
        val listIdsProductsFromBasket = productInfoViewModel.listIdsProductsFromBasket.value
        val result = Bundle().apply {
            putSerializable(KEY_FAVORITE_MODEL, getFavoriteModel())
            putBoolean(KEY_IS_FAVORITES, isFavorite)
            putIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FROM_BASKET,listIdsProductsFromBasket?.toArrayListInt())
        }

        getSupportActivity().setFragmentResult(requestKey = KEY_RESULT_FROM_PRODUCT_INFO, result = result)

        listener()
    }

    /**
     * Заполнение layoutBasicInfo основной информацией о товаре.
     *
     * Параметры:
     * [list] - список из пар ключ-значение, где ключ это заголовок, а значение это информация по этому заголовку.
     */
    private fun installBasicInfo(list:List<Map<String,String>>) = with(binding){
        // Отчистка layout на случай если в нем уже была информация
        layoutBasicInfo.removeAllViews()

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

            /*
            Создание LinearLayout с горизонтальной ориентацией, который будет хранить два TextView -
            заголовок основной информации и содержание основной информации
             */
            val newHorizontalLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                (layoutParams as LinearLayout.LayoutParams).setMargins(0, 32, 0, 8)

            }

            // Создание разделителя, который будет находиться между элементами информации
            val divider = View(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    3
                )
                val colorDivider = resources.getColor(R.color.gray300, resources.newTheme())

                setBackgroundColor(colorDivider)
            }

            // Создание TextView для заголовка
            val newTextViewTitle = TextView(requireContext()).apply {
                text = detailsProductModel.title
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setTextAppearance(android.R.style.TextAppearance_Material_Body2)
            }

            // Создание TextView для содержимого
            val newTextViewBody = TextView(requireContext()).apply {
                text = detailsProductModel.body

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )

                setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            }

            // Добавление созданных view в layoutBasicInfo
            newHorizontalLayout.addView(newTextViewTitle)
            newHorizontalLayout.addView(newTextViewBody)
            layoutBasicInfo.addView(newHorizontalLayout)

            // Проверка на последний элемент. Последний элемент не должен иметь разделитель
            if (index < numberLines) layoutBasicInfo.addView(divider)
        }
    }

    /**
     * Заполнение списка [arrayListTitles] заголовками, а списка [arrayListBody] самими инстркукциями,
     * для передачи на InstructionManualFragment.
     *
     * Параметры:
     * [list] - список из пар ключ-значение, где ключ это заголовок, а значение это информация по этому заголовку.
     */
    private fun fillingInstructions(list:List<Map<String,String>>) {

        list.forEach { map ->
            map.forEach { key, value ->
                arrayListTitles.add(key)
                arrayListBody.add(value)
            }
        }

    }

}