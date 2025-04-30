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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.models.ButtonModel
import com.example.domain.models.DetailsProductModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.ColorUtils
import com.example.pharmacyapp.FLAG_CURRENT_PRODUCT
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_BODY_INSTRUCTION
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP
import com.example.pharmacyapp.KEY_ARRAY_LIST_TITLES_INSTRUCTION
import com.example.pharmacyapp.KEY_FLAGS_FOR_MAP
import com.example.pharmacyapp.KEY_IS_FAVORITES
import com.example.pharmacyapp.KEY_IS_IN_BASKET
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_RESULT_FROM_PRODUCT_INFO
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID
import com.example.pharmacyapp.TYPE_GET_PRODUCT_BY_ID
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentProductInfoBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductInfoViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.ProductInfoViewModelFactory
import kotlinx.coroutines.launch
import java.lang.Exception


/**
 * Класс [ProductInfoFragment] отвечает за отрисовку и работу экрана подробной информации о товаре.
 */
class ProductInfoFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentProductInfoBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productInfoViewModel: ProductInfoViewModel by viewModels(
        factoryProducer = { ProductInfoViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerCatalog: NavController

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var buttonModel: ButtonModel

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(productInfoViewModel){

            sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES,Context.MODE_PRIVATE)

            initValues(
                userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER),
                productId = arguments?.getInt(KEY_PRODUCT_ID)
            )

            val colorUtils = ColorUtils(context = requireContext())

            val colorPrimary = colorUtils.colorPrimary
            val colorSecondaryContainer = colorUtils.colorSecondaryContainer
            val colorOnSecondaryContainer = colorUtils.colorOnSecondaryContainer
            val colorOnPrimary = colorUtils.colorOnPrimary
            buttonModel = ButtonModel(
                colorPrimary = colorPrimary,
                colorOnPrimary = colorOnPrimary,
                colorSecondaryContainer = colorSecondaryContainer,
                colorOnSecondaryContainer = colorOnSecondaryContainer,
                textPrimary = getString(R.string.add_to_the_shopping_cart),
                textSecondary = getString(R.string.in_the_shopping_cart)
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
                    productModel.collect{ productModel ->
                        if (productModel != null) installProductModel(productModel = productModel)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    listProductAvailability.collect{ listProductAvailability ->
                        installProductAvailability(listProductAvailability = listProductAvailability)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    listIdsProductsFromBasket.collect{ listIdsProductsFromBasket ->
                        installButtonInBasket(listIdsProductsFromBasket = listIdsProductsFromBasket)
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED){
                    listAllFavorite.collect{ listAllFavorite ->
                        installMenu(listAllFavorite = listAllFavorite)
                    }
                }
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

        navControllerMain = getSupportActivity().getNavControllerMain()

        navControllerCatalog = findNavController()
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

        // Обработка нажатия на картику товара
        ivProductInfo.setOnClickListener {
            productInfoViewModel.onClickImageProduct { image ->
                // Установка картинки товара для передачи на экран с полным изображение
                val bundle = Bundle().apply {
                    putString(FullImageProductFragment.KEY_FULL_IMAGE_PRODUCT,image)
                }
                // Открытие экрана с полным изображением товара
                navControllerMain.navigate(R.id.fullImageProductFragment, bundle)
            }
        }

        // Обработка нажатия кнопки "В корзину"
        bInBasket.setOnClickListener {
            onClickInBasket()
        }

        // Обработка нажатия на поле "Наличие"
        cardAvailability.setOnClickListener {
            productInfoViewModel.onClickCardAvailability { arrayListIdsProducts, arrayListNumberProducts ->

                val bundle = Bundle().apply {
                    putString(KEY_FLAGS_FOR_MAP, FLAG_CURRENT_PRODUCT)
                    putIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP,arrayListIdsProducts)
                    putIntegerArrayList(KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP,arrayListNumberProducts)
                }

                // Открытие экрана карты с аптеками
                navControllerMain.navigate(R.id.mapFragment, bundle)
            }
        }

        // Обработка нажатия кнопки "Прпробовать снова"
        layoutPendingResultProductInfo.bTryAgain.setOnClickListener {
            productInfoViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        // Обработка нажатия на поле "Инструкция по применению"
        cardInstruction.setOnClickListener {
            productInfoViewModel.onClickCardInstruction { arrayListTitles, arrayListBody ->
                // Открытие экрана с инструкцие по применению
                val bundle = Bundle()
                bundle.apply {
                    putStringArrayList(KEY_ARRAY_LIST_TITLES_INSTRUCTION, arrayListTitles)
                    putStringArrayList(KEY_ARRAY_LIST_BODY_INSTRUCTION, arrayListBody)
                }

                navControllerMain.navigate(R.id.instructionManualFragment,bundle)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("TAG","ProductInfoFragment onDestroyView")
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","ProductInfoFragment onSuccessResultListener")
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
                TYPE_GET_PRODUCT_BY_ID + TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID + TYPE_GET_IDS_PRODUCTS_FROM_BASKET + TYPE_GET_ALL_FAVORITES-> {
                    Log.i("TAG","fullType = TYPE_GET_PRODUCT_BY_ID + TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID + TYPE_GET_IDS_PRODUCTS_FROM_BASKET + TYPE_GET_ALL_FAVORITES")
                    val resultGetProductById = listRequests.find { it.type == TYPE_GET_PRODUCT_BY_ID }?.result!!.asSuccess()!!
                    val resultGetProductAvailabilityByProductId = listRequests.find { it.type == TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID }?.result!!.asSuccess()!!
                    val resultGetIdsProductsFromBasket = listRequests.find { it.type == TYPE_GET_IDS_PRODUCTS_FROM_BASKET }?.result!!.asSuccess()!!
                    val resultGetAllFavorites = listRequests.find { it.type == TYPE_GET_ALL_FAVORITES }?.result!!.asSuccess()!!


                    val responseGetProductById = resultGetProductById.data as ResponseValueModel<*>
                    val responseGetProductAvailabilityByProductId = resultGetProductAvailabilityByProductId.data as ResponseValueModel<*>
                    val responseGetIdsProductsFromBasket = resultGetIdsProductsFromBasket.data as ResponseValueModel<*>
                    val responseGetAllFavorites = resultGetAllFavorites.data as ResponseValueModel<*>


                    val produceModel = responseGetProductById.value as ProductModel

                    val _listProductAvailability= responseGetProductAvailabilityByProductId.value as List<*>
                    val listProductAvailability = _listProductAvailability.map { it as ProductAvailabilityModel }

                    val _listIdsProductsFromBasket = responseGetIdsProductsFromBasket.value as List<*>
                    val listIdsProductsFromBasket = _listIdsProductsFromBasket.map { it as Int }

                    val _listAllFavorite = responseGetAllFavorites.value as List<*>
                    val listAllFavorite = _listAllFavorite.map { it as FavoriteModel }

                    productInfoViewModel.fillData(
                        productModel = produceModel,
                        listProductAvailability = listProductAvailability,
                        listIdsProductsFromBasket = listIdsProductsFromBasket,
                        listAllFavorite = listAllFavorite
                    )

                }
                TYPE_ADD_FAVORITE -> { productInfoViewModel.changeListAllFavorite() }
                TYPE_REMOVE_FAVORITES -> { productInfoViewModel.changeListAllFavorite() }
                TYPE_ADD_PRODUCT_IN_BASKET -> { productInfoViewModel.changeListIdsProductsFromBasket() }
                TYPE_DELETE_PRODUCT_FROM_BASKET -> { productInfoViewModel.changeListIdsProductsFromBasket() }
            }

            updateMenu()

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        // Отчистка меню
        toolbarViewModel.clearMenu()
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        toolbarViewModel.clearMenu()
        updateUI(flag = FLAG_PENDING_RESULT)
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

    private fun installMenu(listAllFavorite: List<FavoriteModel>){
        Log.d("TAG","installMenu")
        productInfoViewModel.installMenu(listAllFavorite = listAllFavorite){ isFavorite ->
            // Установка меню
            toolbarViewModel.inflateMenu(menu = if (isFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
            toolbarViewModel.setMenuClickListener { itemId ->
                onClickMenuItem(itemId = itemId)
            }
        }
    }

    private fun updateMenu(){
        Log.d("TAG","updateMenu")
        productInfoViewModel.updateMenu(){ isFavorite ->
            toolbarViewModel.inflateMenu(menu = if (isFavorite) R.menu.menu_favorite else R.menu.menu_favorite_border)
            toolbarViewModel.setMenuClickListener { itemId ->
                onClickMenuItem(itemId = itemId)
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
        productInfoViewModel.installProductModel(productModel = productModel){ productInfoModel ->
            with(productInfoModel){
                ivProductInfo.load(image)

                tvProductNameInfo.text = title

                tvPriceWithClubCardInfo.text = textPriceClub

                tvPriceInfo.text = textPrice
                tvOriginalPriceInfo.text = textOriginalPrice
                tvOriginalPriceInfo.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                tvDiscountInfo.text = textDiscount

                if (isDiscount) {
                    cardDiscount.visibility = View.VISIBLE
                    tvOriginalPriceInfo.visibility = View.VISIBLE
                    tvRubleSignForOriginalPrice.visibility = View.VISIBLE
                }
                else {
                    cardDiscount.visibility = View.GONE
                    tvOriginalPriceInfo.visibility = View.GONE
                    tvRubleSignForOriginalPrice.visibility = View.GONE
                }
            }
        }

        // Заполнените блока базовой информацие о товаре
        installBasicInfo(list = productModel.productBasicInfo)
    }

    /**
     * Установка наличия товара в аптеках.
     *
     * Параметры:
     * [listProductAvailability] - список наличия товара в аптеках.
     */
    private fun installProductAvailability(listProductAvailability: List<ProductAvailabilityModel>) = with(binding) {
        productInfoViewModel.installProductAvailability(
            listProductAvailability = listProductAvailability,
            textAvailableIn = getString(R.string.available_in),
            textPharmacies = getString(R.string.pharmacies),
            textPharmacy = getString(R.string.pharmacy),
            textOutOfStock = getString(R.string.out_of_stock)
        ){ textNumberPharmaciesWithProduct ->

            tvNumberPharmaciesWithProduct.text = textNumberPharmaciesWithProduct
        }
    }

    /**
     * Установка внешнего вида кнопки "В корзину".
     *
     * Параметры:
     * [listIdsProductsFromBasket] - список идентификаторов товаров из корзины.
     */
    private fun installButtonInBasket(listIdsProductsFromBasket: List<Int>) = with(binding){
        productInfoViewModel.installButtonInBasket(listIdsProductsFromBasket = listIdsProductsFromBasket, buttonModel = buttonModel){ button ->
            with(button){
                bInBasket.setBackgroundColor(colorBackground)
                bInBasket.setTextColor(colorText)
                bInBasket.text = text
            }
        }
    }

    /**
     * Обработка нажатия на кнопку "В корзину".
     */
    private fun onClickInBasket() {
        productInfoViewModel.onClickInBasket(isNetworkStatus = isNetworkStatus){
            navControllerMain.navigate(R.id.nav_graph_log_in)
        }
    }

    /**
     * Обработка нажатия на кнопку в меню.
     *
     * Параметры:
     * [itemId] - идентификатор, нажатого элемента.
     */
    private fun onClickMenuItem(itemId: Int) {
        productInfoViewModel.onClickFavorite(isNetworkStatus = isNetworkStatus, itemId = itemId){
            navControllerMain.navigate(R.id.nav_graph_log_in)
        }
    }

    private fun onBack(back: () -> Unit) {
        productInfoViewModel.onBack { isFavorite, isInBasket, productId ->
            val result = Bundle().apply {
                putInt(KEY_PRODUCT_ID,productId)
                putBoolean(KEY_IS_FAVORITES, isFavorite)
                putBoolean(KEY_IS_IN_BASKET,isInBasket)
            }
            getSupportActivity().setFragmentResult(requestKey = KEY_RESULT_FROM_PRODUCT_INFO, result = result)
        }
        back()
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
}