package com.example.pharmacyapp.tabs.catalog

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.catalog.CatalogResult
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductFavoriteModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.ButtonModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.ColorUtils
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_ARRAY_LIST_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.KEY_ARRAY_LIST_SELECTED_ADDRESSES
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_FROM
import com.example.pharmacyapp.KEY_DEFAULT_PRICE_UP_TO
import com.example.pharmacyapp.KEY_FAVORITE_MODEL
import com.example.pharmacyapp.KEY_IS_CHECKED_DISCOUNT
import com.example.pharmacyapp.KEY_IS_FAVORITES
import com.example.pharmacyapp.KEY_PATH
import com.example.pharmacyapp.KEY_PRICE_FROM
import com.example.pharmacyapp.KEY_PRICE_UP_TO
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_RESULT_ARRAY_LIST_IDS_FILTERED
import com.example.pharmacyapp.KEY_RESULT_FROM_PRODUCT_INFO
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentProductsBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getPrice
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.sortingByDiscountAmount
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_ASCENDING_PRICE
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_DESCENDING_PRICE
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_DISCOUNT_AMOUNT
import com.example.pharmacyapp.tabs.catalog.adapters.ProductsAdapter
import com.example.pharmacyapp.tabs.catalog.viewmodels.ProductsViewModel
import com.example.pharmacyapp.tabs.catalog.viewmodels.factories.ProductsViewModelFactory
import kotlin.properties.Delegates


/**
 * Класс [ProductsFragment] является экраном со списокм товаров по категориям.
 * Список товаров может быть отфильтрованным и отсортированным по определенным значениям.
 * Также нажимая на товар на соответствующие кнопки можно добавить (или удалить) его в список избранного или в корзину.
 */
class ProductsFragment : Fragment(), CatalogResult {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val productsViewModel: ProductsViewModel by viewModels(
        factoryProducer = { ProductsViewModelFactory(context = requireContext()) }
    )

    private lateinit var navControllerCatalog: NavController
    private lateinit var navControllerMain: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private var userId by Delegates.notNull<Int>()

    private var path by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        // Получение id пользователя
        userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        // Получение пути по которому будет получен список товаров
        path = arguments?.getString(KEY_PATH)?:
        throw NullPointerException("ProductsFragment path = null")

        // Отправка всех начальных запросов
        sendingRequests()

        /*
          Получение результата с экрана ProductInfoFragment.
          Результат:
          isFavorite - значение того в "Избранном" товар или нет;
          favoriteModel - данные товара в виде модели избранного для изменения списка избранных товаров;
          arrayListIdsProductsFromBasket - список идентификаторов товаров, находящихся в корзину.
          Нужен для того чтобы корректно отображать элемент списка товаров.
         */
        getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_FROM_PRODUCT_INFO) { requestKey, bundle ->

            // Установка значения для отрисовки адаптера.
            // При изменении определенных значений адаптер перерисуется заново.
            productsViewModel.setIsShownProductsAdapter(isShown = false)

            val isFavorite = bundle.getBoolean(KEY_IS_FAVORITES)

            val favoriteModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                 bundle.getSerializable(KEY_FAVORITE_MODEL, FavoriteModel::class.java)
            }
            else {
                bundle.getSerializable(KEY_FAVORITE_MODEL) as FavoriteModel?
            }
                ?: throw NullPointerException("ProductsFragment favoriteModel = null")

            val arrayListIdsProductsFromBasket = bundle.getIntegerArrayList(KEY_ARRAY_LIST_IDS_PRODUCTS_FROM_BASKET)?:
            throw NullPointerException("ProductsFragment arrayListIdsProductsFromBasket = null")

            // Установка нового списка идентификаторов товаров из корзины пользователя
            productsViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = arrayListIdsProductsFromBasket.toList())

            // Изменение списка "Избранного"
            productsViewModel.changeListAllFavorites(favoriteModel = favoriteModel, isFavorite = isFavorite)

        }

        /*
          Получение результата с экрана FilterFragment.
          Результат:
          arrayListIdsFiltered - список отфильтрованных идентификаторов товаров;
          isChecked - значение для фильтрование товары со скидкой;
          priceFrom - цена от;
          priceUpTo - цена до;
          arrayListIdsSelectedAddresses - выбранные адреса аптек.
         */
        getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_ARRAY_LIST_IDS_FILTERED) { requestKey, bundle ->

            productsViewModel.setIsShownProductsAdapter(isShown = false)

            val arrayListIdsFiltered = bundle.getIntegerArrayList(KEY_ARRAY_LIST_IDS_FILTERED) ?: arrayListOf<Int>()
            val isChecked = bundle.getBoolean(KEY_IS_CHECKED_DISCOUNT)
            val priceFrom = bundle.getInt(KEY_PRICE_FROM)
            val priceUpTo = bundle.getInt(KEY_PRICE_UP_TO)
            val arrayListIdsSelectedAddresses = bundle.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

            arguments?.putBoolean(KEY_IS_CHECKED_DISCOUNT,isChecked)
            arguments?.putInt(KEY_PRICE_FROM,priceFrom)
            arguments?.putInt(KEY_PRICE_UP_TO,priceUpTo)
            arguments?.putIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES,arrayListIdsSelectedAddresses)

            Log.i("TAG","ProductsFragment onCreate arrayListIdsFiltered = $arrayListIdsFiltered")
            Log.i("TAG","ProductsFragment onCreate isChecked = $isChecked")
            Log.i("TAG","ProductsFragment onCreate priceFrom = $priceFrom")
            Log.i("TAG","ProductsFragment onCreate priceUpTo = $priceUpTo")
            Log.i("TAG","ProductsFragment onCreate arrayListIdsSelectedAddresses = $arrayListIdsSelectedAddresses")

            val defaultPriceFrom = arguments?.getInt(KEY_DEFAULT_PRICE_FROM) ?:
            throw NullPointerException("ProductsFragment defaultPriceFrom = null")

            val defaultPriceUpTo = arguments?.getInt(KEY_DEFAULT_PRICE_UP_TO) ?:
            throw NullPointerException("ProductsFragment defaultPriceUpTo = null")

            val listAllProducts = productsViewModel.listAllProducts.value ?:
            throw NullPointerException("ProductsFragment listAllProducts = null")

            val listFilteredProduct = listAllProducts.filter { productModel ->
                arrayListIdsFiltered.contains(productModel.productId)
            }

            // Проверка применены ли фильтры
            val isCheckedFilter = if (
                priceFrom == defaultPriceFrom &&
                priceUpTo == defaultPriceUpTo &&
                arrayListIdsFiltered.size == listAllProducts.size &&
                arrayListIdsSelectedAddresses.size == 0 &&
                !isChecked
                ) false else true

            // Установка значения - применены фильтры или нет
            productsViewModel.setIsCheckFilter(isChecked = isCheckedFilter)

            with(SortingBottomSheetDialogFragment) {
                // Получаени типы сортировка
                val typeSort = arguments?.getInt(KEY_SORTING_TYPE, SORT_ASCENDING_PRICE)?: SORT_ASCENDING_PRICE

                // Получение отсортированного по типу списка
                val sortedListProducts = sortListProducts(
                    typeSort = typeSort,
                    listProducts = listFilteredProduct
                )

                // Изменение списка товаров
                productsViewModel.setListProductsModel(listProductModel = sortedListProducts)
            }

        }

        /*
          Получение результата с экрана SortingBottomSheetDialogFragment.
          Результат:
          type - тип сортировки.
         */
        with(SortingBottomSheetDialogFragment) {
            getSupportActivity().setFragmentResultListener(requestKey = KEY_RESULT_SORTING_PRODUCTS) { requestKey, bundle ->

                productsViewModel.setIsShownProductsAdapter(isShown = false)

                val type = bundle.getInt(KEY_SORTING_TYPE)
                arguments?.putInt(KEY_SORTING_TYPE,type)

                val listProducts = productsViewModel.listProducts.value ?:
                throw NullPointerException("ProductsFragment listProducts = null")

                val sortedListProducts = sortListProducts(
                    typeSort = type,
                    listProducts = listProducts
                )

                // Установка отсортированного списка
                productsViewModel.setListProductsModel(listProductModel = sortedListProducts)

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
            with(productsViewModel) {

                // Установка значение "не показано" для всех запросов
                setIsShownGetProductsByPath(isShown = false)
                setIsShownGetAllFavorites(isShown = false)
                setIsShownGetIdsProductsFromBasket(isShown = false)
                setIsShownProductsAdapter(isShown = false)

                // Повторный вызов всех запросов
                sendingRequests()
            }
        }

        // Обработка кнопки "Фильтры"
        bFilters.setOnClickListener {

            // Получение списка всех товаров
            val listAllProducts = productsViewModel.listAllProducts.value ?:
            throw NullPointerException("ProductsFragment listAllProducts = null")

            // Получение списка цен товаров
            val listPrices = getListPrices(listAllProducts = listAllProducts)

            // Минимальная цена
            val defaultPriceFrom = listPrices.min()

            // Максимиальная цена
            val defaultPriceUpTo = listPrices.max()

            // Установка минимальной и максимальной цены
            arguments?.putInt(KEY_DEFAULT_PRICE_FROM,defaultPriceFrom)
            arguments?.putInt(KEY_DEFAULT_PRICE_UP_TO,defaultPriceUpTo)

            /*
              Поучаение значений фильтров на тот случай если
              они уже были применены и их надо снова передать на экран FilterFragment.
              Если это первый переход  на экран FilterFragment, то будут переданы значения по умолчанию.
             */
            val isChecked = arguments?.getBoolean(KEY_IS_CHECKED_DISCOUNT) ?: false
            val priceFrom = arguments?.getInt(KEY_PRICE_FROM) ?: -1
            val priceUpTo = arguments?.getInt(KEY_PRICE_UP_TO) ?: -1
            val arrayListIdsSelectedAddresses = arguments?.getIntegerArrayList(KEY_ARRAY_LIST_SELECTED_ADDRESSES) ?: arrayListOf<Int>()

            Log.i("TAG","ProductsFragment isCheckedFilter = $isChecked")
            Log.i("TAG","ProductsFragment priceFromFilter = $priceFrom")
            Log.i("TAG","ProductsFragment priceUpToFilter = $priceUpTo")
            Log.i("TAG","ProductsFragment defaultPriceFrom = $defaultPriceFrom")
            Log.i("TAG","ProductsFragment defaultPriceUpTo = $defaultPriceUpTo")
            Log.i("TAG","ProductsFragment arrayListIdsSelectedAddressesFilter = $arrayListIdsSelectedAddresses")

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

        // Обработка кнопки "Сортировка"
        bSorting.setOnClickListener {
            with(SortingBottomSheetDialogFragment) {
                // Открытие нижней панели для сортировки и передача туда текущего типа сортировки
                val type = arguments?.getInt(KEY_SORTING_TYPE, SORT_ASCENDING_PRICE) ?: SORT_ASCENDING_PRICE
                val sortingBottomSheetDialogFragment = newInstance(type = type)
                sortingBottomSheetDialogFragment.show(parentFragmentManager,TAG_SORTING_BOTTOM_SHEET)
            }
        }

        // Наблюдение за изменениями listProducts, listAllFavorites, listIdsProductsFromBasket
        productsViewModel.mediatorIsAllRequests.observe(viewLifecycleOwner) {
            // Получение списка товаров, списка всех товаров из "Избранного"
            // и списка идентификаторов товаров из корзины пользователя
            val listProducts = productsViewModel.listProducts.value
            val listAllFavorites = productsViewModel.listAllFavorites.value
            val listIdsProductsFromBasket = productsViewModel.listIdsProductsFromBasket.value

            // Если результаты по всем запросам пришли
            if (
                listProducts != null &&
                listAllFavorites != null &&
                listIdsProductsFromBasket != null
                ) {

                // Установка списка товаров
                installListProducts(
                    listProducts = listProducts,
                    listAllFavorites = listAllFavorites,
                    listIdsProductsFromBasket = listIdsProductsFromBasket
                )

            }
        }

        // Наблюдение за изменение наличия фильтров
        productsViewModel.isCheckFilter.observe(viewLifecycleOwner) { isChecked ->
            // Если фильтры есть то будет отображена точка над кнопкой, если нет, то не будет
            ivCheckFilter.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Наблюдение за получением результатов запросов
        productsViewModel.mediatorProduct.observe(viewLifecycleOwner) { mediatorResult ->
            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener()}
                is SuccessResult -> {
                    onSuccessResultListener(
                        value = result.value,
                        type = type
                    )
                }
                is ErrorResult -> {
                    val errorType = productsViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Установка значения для отрисовки адаптера.
        // При изменении определенных значений адаптер перерисуется заново.
        productsViewModel.setIsShownProductsAdapter(isShown = false)
    }

    override fun onPendingResultListener() {
        Log.i("TAG","ProductsFragment onPendingResultListener")
        // Отчистка типа ошибки
        productsViewModel.clearErrorType()
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun <T> onSuccessResultListener(value: T, type: String?): Unit {
        Log.i("TAG","ProductsFragment onSuccessResultListener")
        when(type) {
            TYPE_GET_PRODUCTS_BY_PATH -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_GET_PRODUCTS_BY_PATH")

                val responseValueModel = value as ResponseValueModel<*>
                val status = responseValueModel.responseModel.status
                val message = responseValueModel.responseModel.message

                val isShownGetProductsByPath = productsViewModel.isShownGetProductsByPath

                Log.i("TAG","responseValueModel = $responseValueModel")
                Log.i("TAG","status = $status")
                if (!isShownGetProductsByPath) {
                    if (status in 200..299){

                        val _listAllProducts = responseValueModel.value as List<*>
                        val listAllProducts = _listAllProducts.map {
                            return@map it as ProductModel
                        }

                        productsViewModel.setListAllProductsModel(listProductModel = listAllProducts.sorted())

                        productsViewModel.setListProductsModel(listProductModel = listAllProducts.sorted())

                    }
                    else {
                        productsViewModel.setResultGetProductsByPath(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                productsViewModel.setIsShownGetProductsByPath(isShown = true)
            }
            TYPE_ADD_FAVORITE -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_ADD_FAVORITE")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    updateUI(flag = FLAG_SUCCESS_RESULT)

                }
                else {
                    productsViewModel.setResultAddFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_REMOVE_FAVORITES -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_REMOVE_FAVORITES")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productsViewModel.setResultRemoveFavorites(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_GET_ALL_FAVORITES -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_GET_ALL_FAVORITES")
                val responseValueModel = value as ResponseValueModel<*>
                val status = responseValueModel.responseModel.status
                val message = responseValueModel.responseModel.message

                val isShownGetAllFavorites = productsViewModel.isShownGetAllFavorites

                if (!isShownGetAllFavorites) {
                    if (status in 200..299) {

                        val _listAllFavorites = responseValueModel.value as List<*>
                        val listAllFavorites = _listAllFavorites.map {
                            return@map it as FavoriteModel
                        }

                        Log.i("TAG","ProductsFragment listAllFavorites = $listAllFavorites")
                        productsViewModel.setListAllFavorites(listAllFavorites = listAllFavorites)

                    }
                    else {
                        productsViewModel.setResultGetAllFavorites(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                productsViewModel.setIsShownGetAllFavorites(isShown = true)

            }
            TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_GET_IDS_PRODUCTS_FROM_BASKET")
                val isShownGetIdsProductsFromBasket = productsViewModel.isShownGetIdsProductsFromBasket

                if (!isShownGetIdsProductsFromBasket) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val status = responseValueModel.responseModel.status
                    val message = responseValueModel.responseModel.message

                    if (status in 200..299) {

                        val _listIdsProductsFromBasket = responseValueModel.value as List<*>
                        val listIdsProductsFromBasket = _listIdsProductsFromBasket.map {
                            return@map it as Int
                        }

                        productsViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = listIdsProductsFromBasket)
                    }
                    else {
                        productsViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                productsViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
            }
            TYPE_ADD_PRODUCT_IN_BASKET -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_ADD_PRODUCT_IN_BASKET")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    val productId = productsViewModel.currentProductId.value

                    if (productId != null) {

                        val listIdsProductsFromBasket = productsViewModel.listIdsProductsFromBasket.value?:
                        throw NullPointerException("ProductsFragment listIdsProductsFromBasket = null")

                        val mutableListIdsProductsFromBasket = listIdsProductsFromBasket.toMutableList()

                        mutableListIdsProductsFromBasket.add(productId)

                        productsViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = mutableListIdsProductsFromBasket)
                    }

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productsViewModel.setResultAddProductInBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
            TYPE_DELETE_PRODUCT_FROM_BASKET -> {
                Log.i("TAG","ProductsFragment onSuccessResultListener TYPE_DELETE_PRODUCT_FROM_BASKET")
                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {
                    val productId = productsViewModel.currentProductId.value

                    if (productId != null) {

                        val listIdsProductsFromBasket = productsViewModel.listIdsProductsFromBasket.value?:
                        throw NullPointerException("ProductsFragment listIdsProductsFromBasket = null")

                        val mutableListIdsProductsFromBasket = listIdsProductsFromBasket.toMutableList()

                        mutableListIdsProductsFromBasket.remove(productId)

                        productsViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = mutableListIdsProductsFromBasket)
                    }

                    updateUI(flag = FLAG_SUCCESS_RESULT)
                }
                else {
                    productsViewModel.setResultDeleteProductFromBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","ProductsFragment onErrorResultListener")
        updateUI(FLAG_ERROR_RESULT, messageError = message)
        // Установка значения "не показано" для всех запросов
        productsViewModel.setIsShownGetProductsByPath(isShown = true)
        productsViewModel.setIsShownGetAllFavorites(isShown = true)
        productsViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
        productsViewModel.setIsShownProductsAdapter(isShown = true)
    }

    override fun onSuccessfulEvent(type: String, exception: Exception?, onSuccessfulEventListener:() -> Unit){
        val isNetworkStatus = getSupportActivity().isNetworkStatus(context = requireContext())
        val network = Network()

        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                when(type) {
                    TYPE_GET_PRODUCTS_BY_PATH -> productsViewModel.setResultGetProductsByPath(result = PendingResult())
                    TYPE_GET_ALL_FAVORITES -> productsViewModel.setResultGetAllFavorites(result = PendingResult())
                    TYPE_ADD_FAVORITE -> productsViewModel.setResultAddFavorite(result = PendingResult())
                    TYPE_REMOVE_FAVORITES -> productsViewModel.setResultRemoveFavorites(result = PendingResult())
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> productsViewModel.setResultGetIdsProductsFromBasket(result = PendingResult())
                    TYPE_ADD_PRODUCT_IN_BASKET -> productsViewModel.setResultAddProductInBasket(result = PendingResult())
                    TYPE_DELETE_PRODUCT_FROM_BASKET -> productsViewModel.setResultDeleteProductFromBasket(result = PendingResult())
                }
                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()
                when(type) {
                    TYPE_GET_PRODUCTS_BY_PATH -> productsViewModel.setResultGetProductsByPath(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_ALL_FAVORITES -> productsViewModel.setResultGetAllFavorites(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_ADD_FAVORITE -> productsViewModel.setResultAddFavorite(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_REMOVE_FAVORITES -> productsViewModel.setResultRemoveFavorites(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> productsViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_ADD_PRODUCT_IN_BASKET -> productsViewModel.setResultAddProductInBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_DELETE_PRODUCT_FROM_BASKET -> productsViewModel.setResultDeleteProductFromBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                }
                getSupportActivity().showToast(message = getString(R.string.check_your_internet_connection))
            }
        )
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
     * Отправка всех, необходимых для работы экрана, запросов.
     */
    private fun sendingRequests() {

        val isShownGetAllFavorites = productsViewModel.isShownGetAllFavorites
        val isShownGetProductsByPath = productsViewModel.isShownGetProductsByPath
        val isShownGetIdsProductsFromBasket = productsViewModel.isShownGetIdsProductsFromBasket

        if (!isShownGetAllFavorites) {
            onSuccessfulEvent(type = TYPE_GET_ALL_FAVORITES) {
                productsViewModel.getAllFavorites()
            }
        }

        if (!isShownGetProductsByPath) {
            with(productsViewModel) {
                onSuccessfulEvent(type = TYPE_GET_PRODUCTS_BY_PATH) {
                    getProductsByPath(path = path)
                }
            }
        }

        if (!isShownGetIdsProductsFromBasket) {
            with(productsViewModel) {
                onSuccessfulEvent(type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET) {
                    getIdsProductsFromBasket(userId = userId)
                }
            }
        }
    }

    /**
     * Обработка нажатия на товар.
     * Открытие экрана подробной информации о товаре.
     *
     * Параметры:
     * [productId] - идентификатор, нажатого товара;
     * [isFavorite] - значение того находится ли товар в "Избранном".
     */
    private fun onClickProduct(productId: Int, isFavorite: Boolean) {

        val bundle = Bundle().apply {
            putInt(KEY_PRODUCT_ID, productId)
            putBoolean(KEY_IS_FAVORITES, isFavorite)
        }

        navControllerCatalog.navigate(R.id.action_productsFragment_to_productInfoFragment, bundle)
    }

    /**
     * Обработка нажатия на "сердечко".
     * Добавление и удаление из списка избранного в зависимовти от значения [isFavorite]
     * (true - добавление, false - удаление).
     *
     * Праметры:
     * [favoriteModel] - модель избранного, нажатого товара;
     * [isFavorite] - значение того находится ли товар в "Избранном".
     */
    private fun onClickFavorite(favoriteModel: FavoriteModel, isFavorite: Boolean) {

        // Если пользователь не авторизован, то открытие экрана авторизации
        if (userId == UNAUTHORIZED_USER) {
            navControllerMain.navigate(R.id.nav_graph_log_in)
            return
        }

        if (isFavorite) {
            // Добавление
            onSuccessfulEvent(type = TYPE_ADD_FAVORITE) {
                productsViewModel.addFavorite(favoriteModel = favoriteModel)
            }
        }
        else {
            // Удаление
            onSuccessfulEvent(type = TYPE_REMOVE_FAVORITES) {
                productsViewModel.removeFavorite(productId = favoriteModel.productId)
            }
        }

        // Изменение списка избранного
        productsViewModel.changeListAllFavorites(favoriteModel = favoriteModel,isFavorite = isFavorite)

    }

    /**
     * Обработка нажатия на кнопку "В корзину".
     * Добавление/удаление товара из корзины.
     *
     * Параметры:
     * [productId] - идентификатор товара;
     * [isInBasket] - значение того находится товар в корзине или нет.
     */
    private fun onClickInBasket(productId:Int, isInBasket: Boolean) {

        // Если пользователь не авторизован, то открытие экрана авторизации
        if (userId == UNAUTHORIZED_USER) {
            val navControllerMain = getSupportActivity().getNavControllerMain()
            navControllerMain.navigate(R.id.nav_graph_log_in)
            return
        }

        // Установка идентификатора текущего товара
        productsViewModel.setCurrentProductId(productId = productId)

        // Добавление/удаление текущего товара из корзины
        if (isInBasket) {
            onSuccessfulEvent(type = TYPE_ADD_PRODUCT_IN_BASKET){
                productsViewModel.addProductInBasket(
                    userId = userId,
                    productId = productId
                )
            }

        }
        else {
            onSuccessfulEvent(type = TYPE_DELETE_PRODUCT_FROM_BASKET){
                productsViewModel.deleteProductFromBasket(
                    userId = userId,
                    productId = productId
                )
            }

        }
        Log.i("TAG","ProductsFragment onClickInBasket\nproductId = $productId\nisInBasket = $isInBasket")

    }

    /**
     * Сортировка списка товаров по типу.
     *
     * Параметры:
     * [typeSort] - тип сортировки(
     * [SORT_ASCENDING_PRICE] - по возрастанию цены,
     * [SORT_DESCENDING_PRICE] - по убыванию цены,
     * [SORT_DISCOUNT_AMOUNT] - по величине скидки
     * );
     * [listProducts] - список товаров для сортировки.
     */
    private fun sortListProducts(typeSort: Int, listProducts: List<ProductModel>): List<ProductModel> {

        val sortedListProducts: List<ProductModel> = when (typeSort) {
            SORT_ASCENDING_PRICE -> {
                listProducts.sorted()
            }
            SORT_DESCENDING_PRICE -> {
                listProducts.sortedDescending()
            }
            SORT_DISCOUNT_AMOUNT -> {
                listProducts.sortingByDiscountAmount()
            }
            else -> throw IllegalArgumentException()
        }

        return sortedListProducts
    }

    /**
     * Получение спика цен с применением скидок.
     *
     * Параметры:
     * [listAllProducts] - список всех товаров.
     */
    private fun getListPrices(listAllProducts: List<ProductModel>): List<Int> {
        val listPrices = listAllProducts.map {
            val productModel = it

            return@map getPrice(
                discount = productModel.discount,
                price = productModel.price
            )
        }

        return listPrices

    }

    /**
     * Установка и настройка списка товаров для адаптера.
     *
     * Параметры:
     * [listProducts] - список товаров;
     * [listAllFavorites] - список, избранных товаров;
     * [listIdsProductsFromBasket] - список идентификаторов товаров из корзины.
     */
    private fun installListProducts(
        listProducts: List<ProductModel>,
        listAllFavorites: List<FavoriteModel>,
        listIdsProductsFromBasket: List<Int>
    ) = with(binding) {

        // Установка модели кнопки
        val colorUtils = ColorUtils(context = requireContext())

        val buttonModel = ButtonModel(
            colorPrimary = colorUtils.colorPrimary,
            colorOnPrimary = colorUtils.colorOnPrimary,
            colorSecondaryContainer = colorUtils.colorSecondaryContainer,
            colorOnSecondaryContainer = colorUtils.colorOnSecondaryContainer,
            textPrimary = getString(R.string.add_to_the_shopping_cart),
            textSecondary = getString(R.string.in_the_shopping_cart)
        )

        // Получение размеры списка всех товаров
        val sizeListAllProducts = productsViewModel.listAllProducts.value?.size ?:
        throw NullPointerException("ProductsFragment sizeListAllProducts = null")

        // Если список товаров пуст, то отображается текст - "Список пуст" иначе отображается список
        if (listProducts.isEmpty()) {
            tvEmptyList.visibility = View.VISIBLE
            rvProducts.visibility = View.GONE
        }
        else {
            tvEmptyList.visibility = View.GONE
            rvProducts.visibility = View.VISIBLE
        }

        // Если товаров по ,выбранной категории, вообще нет, то панель с кнопками "Сортировать" и "Фильтровать" не отображается
        if (sizeListAllProducts == 0) {
            layoutConfigurationPanel.visibility = View.GONE
        }
        else {
            layoutConfigurationPanel.visibility = View.VISIBLE
        }

        // Создание списка, который будет передан в адаптер
        val currentMutableListProductsFavorites = mutableListOf<ProductFavoriteModel>()

        // Заполнение списка currentMutableListProductsFavorites
        listProducts.forEach { productModel ->
            val isFavorite = listAllFavorites.any { favoriteModel ->
                favoriteModel.productId == productModel.productId
            }
            val isInBasket = listIdsProductsFromBasket.any { productId ->
                productId == productModel.productId
            }

            currentMutableListProductsFavorites.add(
                ProductFavoriteModel(
                    isFavorite = isFavorite,
                    productModel = productModel,
                    isInBasket = isInBasket
                )
            )
        }

        // Создание адаптера
        val productsAdapter = ProductsAdapter(
            userId = userId,
            listProducts = currentMutableListProductsFavorites,
            onClickProduct = ::onClickProduct,
            onClickFavorite = ::onClickFavorite,
            onClickInBasket = ::onClickInBasket,
            buttonModel = buttonModel
        )

        // Установка адаптера если он не был уже показан
        val isShownProductsAdapter = productsViewModel.isShownProductsAdapter

        if (!isShownProductsAdapter) {

            updateUI(flag = FLAG_SUCCESS_RESULT)
            rvProducts.adapter = productsAdapter
            rvProducts.layoutManager = GridLayoutManager(requireContext(),2)

            productsViewModel.setIsShownProductsAdapter(isShown = true)
        }

    }
}