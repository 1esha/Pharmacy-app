package com.example.pharmacyapp.tabs.catalog

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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductInCatalogModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.ButtonModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.ColorUtils
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_FROM
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_UP_TO
import com.example.pharmacyapp.KEY_FLAGS_FOR_PRODUCTS
import com.example.pharmacyapp.KEY_IS_CHECKED_DISCOUNT
import com.example.pharmacyapp.KEY_IS_FAVORITES
import com.example.pharmacyapp.KEY_IS_IN_BASKET
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_PRICE_FROM
import com.example.pharmacyapp.KEY_PRICE_UP_TO
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_RESULT_FROM_PRODUCT_INFO
import com.example.pharmacyapp.KEY_SEARCH_TEXT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentProductsBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.catalog.adapters.ProductsAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductsViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductsViewModel.Companion.TYPE_GET_PRODUCTS
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.ProductsViewModelFactory
import kotlinx.coroutines.launch


/**
 * Класс [ProductsFragment] является экраном со списокм товаров по категориям.
 * Список товаров может быть отфильтрованным и отсортированным по определенным значениям.
 * Также нажимая на товар на соответствующие кнопки можно добавить (или удалить) его в список избранного или в корзину.
 */
class ProductsFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productsViewModel: ProductsViewModel by viewModels(
        factoryProducer = { ProductsViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerCatalog: NavController

    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var productsAdapter: ProductsAdapter

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(productsViewModel){

            sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

            initValues(
                flag = arguments?.getString(KEY_FLAGS_FOR_PRODUCTS),
                userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER),
                path = arguments?.getString(KEY_PATH),
                searchText = arguments?.getString(KEY_SEARCH_TEXT)
            )

            getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_FROM_PRODUCT_INFO) { requestKey, bundle ->
                listenResultFromProductInfo(
                    productId = bundle.getInt(KEY_PRODUCT_ID),
                    isFavorite = bundle.getBoolean(KEY_IS_FAVORITES),
                    isInBasket = bundle.getBoolean(KEY_IS_IN_BASKET)
                )
            }

            getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_ARRAY_LIST_IDS_FILTERED) { requestKey, bundle ->
                listenResultFromFilter(
                    isChecked = bundle.getBoolean(KEY_IS_CHECKED_DISCOUNT),
                    priceFrom = bundle.getInt(KEY_PRICE_FROM),
                    priceUpTo = bundle.getInt(KEY_PRICE_UP_TO),
                    arrayListIdsSelectedAddresses = bundle.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES),
                    arrayListIdsFiltered = bundle.getIntegerArrayList(KEY_ARRAY_LIST_IDS_FILTERED) ?: arrayListOf()
                )
            }

            with(SortingBottomSheetDialogFragment) {
                getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_SORTING_PRODUCTS) { requestKey, bundle ->
                    listenResultFromSorting(type = bundle.getInt(KEY_SORTING_TYPE))
                }
            }

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
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    listProductsInCatalog.collect{ listProductsInCatalog ->
                        installUI(mutableListProductsInCatalog = listProductsInCatalog.toMutableList())
                    }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    isCheckFilter.collect{ isChecked ->
                        // Если фильтры есть то будет отображена точка над кнопкой, если нет, то не будет
                        binding.ivCheckFilter.visibility = if (isChecked) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {

        navControllerCatalog = findNavController()

        navControllerMain = getSupportActivity().getNavControllerMain()
        // Установка toolbar
        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.catalog),
            icon = R.drawable.ic_back
        ) { navControllerCatalog.navigateUp()})
        // Отчистка меню
        toolbarViewModel.clearMenu()

        // Обработка кнопки "Прпробовать снова"
        layoutPendingResultProducts.bTryAgain.setOnClickListener {
            productsViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

        // Обработка кнопки "Фильтры"
        bFilters.setOnClickListener {
            productsViewModel.navigateToFilters { path, isChecked, priceFrom, priceUpTo, defaultPriceFrom, defaultPriceUpTo, arrayListIdsSelectedAddresses ->
                // Заполнение значениями для передачи на другой экран
                val bundle = Bundle().apply {
                    putString(KEY_PATH, path)
                    putBoolean(KEY_IS_CHECKED_DISCOUNT,isChecked)
                    putInt(KEY_PRICE_FROM,priceFrom)
                    putInt(KEY_PRICE_UP_TO,priceUpTo)
                    putInt(KEY_DEFAULT_PRICE_FROM,defaultPriceFrom)
                    putInt(KEY_DEFAULT_PRICE_UP_TO,defaultPriceUpTo)
                    putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListIdsSelectedAddresses)
                }

                // Переход на экран FilterFragment
                navControllerCatalog.navigate(R.id.action_productsFragment_to_filterFragment, bundle)
            }
        }

        // Обработка кнопки "Сортировка"
        bSorting.setOnClickListener {
            with(SortingBottomSheetDialogFragment) {
                // Открытие нижней панели для сортировки и передача туда текущего типа сортировки
                productsViewModel.navigateToSorting { type ->
                    val sortingBottomSheetDialogFragment = newInstance(type = type)
                    sortingBottomSheetDialogFragment.show(parentFragmentManager,TAG_SORTING_BOTTOM_SHEET)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        // Установка значения для отрисовки адаптера.
        productsViewModel.setIsInstallAdapter(isInstallAdapter = true)
    }

    override fun <T> onSuccessResultListener(data:T){
        Log.i("TAG","ProductsFragment onSuccessResultListener")
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
                TYPE_GET_PRODUCTS + TYPE_GET_ALL_FAVORITES + TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> {
                    Log.i("TAG","fullType =  TYPE_GET_PRODUCTS + TYPE_GET_ALL_FAVORITES + TYPE_GET_IDS_PRODUCTS_FROM_BASKET")
                    val resultGetProducts = listRequests.find { it.type == TYPE_GET_PRODUCTS }?.result!!.asSuccess()!!
                    val resultGetAllFavorites = listRequests.find { it.type == TYPE_GET_ALL_FAVORITES }?.result!!.asSuccess()!!
                    val resultGetIdsProductsFromBasket = listRequests.find { it.type == TYPE_GET_IDS_PRODUCTS_FROM_BASKET }?.result!!.asSuccess()!!


                    val responseGetProducts = resultGetProducts.data as ResponseValueModel<*>
                    val responseGetAllFavorites = resultGetAllFavorites.data as ResponseValueModel<*>
                    val responseGetIdsProductsFromBasket = resultGetIdsProductsFromBasket.data as ResponseValueModel<*>

                    val _listAllProducts = responseGetProducts.value as List<*>
                    val listAllProducts = _listAllProducts.map { it as ProductModel }

                    val _listAllFavorite = responseGetAllFavorites.value as List<*>
                    val listAllFavorite = _listAllFavorite.map { it as FavoriteModel }

                    val _listIdsProductsFromBasket = responseGetIdsProductsFromBasket.value as List<*>
                    val listIdsProductsFromBasket = _listIdsProductsFromBasket.map { it as Int }

                    productsViewModel.fillData(
                        listAllProducts = listAllProducts,
                        listAllFavorite = listAllFavorite,
                        listIdsProductsFromBasket = listIdsProductsFromBasket
                    )

                }
                TYPE_ADD_FAVORITE -> {
                    Log.i("TAG","fullType = TYPE_ADD_FAVORITE")
                    productsViewModel.changeListProductsInCatalog()
                }
                TYPE_REMOVE_FAVORITES -> {
                    Log.i("TAG","fullType = TYPE_REMOVE_FAVORITES")
                    productsViewModel.changeListProductsInCatalog()
                }
                TYPE_ADD_PRODUCT_IN_BASKET -> {
                    Log.i("TAG","fullType = TYPE_ADD_PRODUCT_IN_BASKET")
                    productsViewModel.changeListProductsInCatalog()
                }
                TYPE_DELETE_PRODUCT_FROM_BASKET -> {
                    Log.i("TAG","fullType = TYPE_DELETE_PRODUCT_FROM_BASKET")
                    productsViewModel.changeListProductsInCatalog()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onLoadingResultListener() {
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun onErrorResultListener(exception: Exception) {
        val message = getErrorMessage(exception = exception)
        updateUI(FLAG_ERROR_RESULT, messageError = getString(message))
    }

    private fun installUI(mutableListProductsInCatalog: MutableList<ProductInCatalogModel>) = with(binding){
        productsViewModel.installAdapter { userId, isEmptyList,isVisibleConfigurationPane, isVisibleFilter ->
            val colorUtils = ColorUtils(context = requireContext())

            val buttonModel = ButtonModel(
                colorPrimary = colorUtils.colorPrimary,
                colorOnPrimary = colorUtils.colorOnPrimary,
                colorSecondaryContainer = colorUtils.colorSecondaryContainer,
                colorOnSecondaryContainer = colorUtils.colorOnSecondaryContainer,
                textPrimary = getString(R.string.add_to_the_shopping_cart),
                textSecondary = getString(R.string.in_the_shopping_cart)
            )

            layoutConfigurationPanel.visibility = if (isVisibleConfigurationPane) View.VISIBLE else View.GONE

            bFilters.visibility = if (isVisibleFilter) View.VISIBLE else View.GONE

            // Если список товаров пуст, то отображается текст - "Список пуст" иначе отображается список
            if (isEmptyList){
                tvEmptyList.visibility = View.VISIBLE
                rvProducts.visibility = View.GONE
            }
            else {
                tvEmptyList.visibility = View.GONE
                rvProducts.visibility = View.VISIBLE
            }

            productsAdapter = ProductsAdapter(
                userId = userId,
                mutableListProductsInCatalog = mutableListProductsInCatalog,
                onClickProduct = ::onClickProduct,
                onClickFavorite = ::onClickFavorite,
                onClickInBasket = ::onClickInBasket,
                buttonModel = buttonModel
            )
            rvProducts.adapter = productsAdapter
            rvProducts.layoutManager = GridLayoutManager(requireContext(),2)
        }
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultProducts) {
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
     * Обработка нажатия на товар.
     * Открытие экрана подробной информации о товаре.
     *
     * Параметры:
     * [productId] - идентификатор, нажатого товара;
     */
    private fun onClickProduct(productId: Int) {

        val bundle = Bundle().apply {
            putInt(KEY_PRODUCT_ID, productId)
        }

        navControllerCatalog.navigate(R.id.action_productsFragment_to_productInfoFragment, bundle)
    }

    /**
     * Обработка нажатия на "сердечко".
     *
     * Праметры:
     * [favoriteModel] - модель избранного, нажатого товара;
     * [isFavorite] - значение того находится ли товар в "Избранном".
     */
    private fun onClickFavorite(favoriteModel: FavoriteModel, isFavorite: Boolean) {
        productsViewModel.onClickFavorite(
            isNetworkStatus = isNetworkStatus,
            favoriteModel = favoriteModel,
            isFavorite = isFavorite
        ){
            navControllerMain.navigate(R.id.nav_graph_log_in)
        }
    }

    /**
     * Обработка нажатия на кнопку "В корзину".
     *
     * Параметры:
     * [productId] - идентификатор товара;
     * [isInBasket] - значение того находится товар в корзине или нет.
     */
    private fun onClickInBasket(productId:Int, isInBasket: Boolean) {
        productsViewModel.onClickInBasket(
            isNetworkStatus = isNetworkStatus,
            productId = productId,
            isInBasket = isInBasket
        ){
            navControllerMain.navigate(R.id.nav_graph_log_in)
        }
    }
}