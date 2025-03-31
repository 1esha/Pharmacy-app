package com.example.pharmacyapp.tabs.catalog.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.basket.usecases.AddProductInBasketUseCase
import com.example.domain.basket.usecases.DeleteProductFromBasketUseCase
import com.example.domain.basket.usecases.GetIdsProductsFromBasketUseCase
import com.example.domain.catalog.models.ProductInCatalogModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.models.RequestModel
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.getPrice
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.KEY_SORTING_TYPE
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_ASCENDING_PRICE
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_DESCENDING_PRICE
import com.example.pharmacyapp.tabs.catalog.SortingBottomSheetDialogFragment.Companion.SORT_DISCOUNT_AMOUNT
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Класс [ProductsViewModel] является viewModel для класса ProductsFragment.
 */
class ProductsViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    /**
     * Хранит значени для оображения точки над кнопкой "Фильтры".
     * Значение true - фильтры применены, false - нет.
     */
    private val _isCheckFilter = MutableStateFlow<Boolean>(false)
    val isCheckFilter = _isCheckFilter.asStateFlow()

    private val _listProductsInCatalog = MutableStateFlow<List<ProductInCatalogModel>>(mutableListOf())
    val listProductsInCatalog: StateFlow<List<ProductInCatalogModel>> get() = _listProductsInCatalog.asStateFlow()

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isInstallAdapter = true

    private var isInit = true

    private val network = Network()

    private var defaultPriceUpTo: Int = 0
    private var defaultPriceFrom: Int = 0

    private var userId = UNAUTHORIZED_USER
    private var path = EMPTY_STRING
    private var typeSort = SORT_ASCENDING_PRICE

    private var isChecked: Boolean = false
    private var priceFrom: Int = defaultPriceFrom
    private var priceUpTo: Int = defaultPriceUpTo
    private var arrayListIdsSelectedAddresses: ArrayList<Int> = arrayListOf()

    private var listAllProductsInCatalog = listOf<ProductInCatalogModel>()

    private var currentProductInCatalog: ProductInCatalogModel? = null

    private var listAllProducts = listOf<ProductModel>()

    fun initValues(
        userId: Int,
        path: String?
    ){
        try {
            if (isInit){
                this.userId = userId
                this.path = path?:throw NullPointerException()

                isInit = false
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }


    fun sendingRequests(isNetworkStatus: Boolean){
        if (isShownSendingRequests) {
            network.checkNetworkStatus(
                isNetworkStatus = isNetworkStatus,
                connectionListener = {

                    onLoading()

                    if (path == EMPTY_STRING){
                        _stateScreen.value = Result.Error(exception = IllegalArgumentException())
                        return@checkNetworkStatus
                    }

                    val getProductsByPathUseCase = GetProductsByPathUseCase(
                        catalogRepository = catalogRepositoryImpl,
                        path = path
                    )
                    val getAllFavoritesUseCase = GetAllFavoritesUseCase(
                        favoriteRepository = favoriteRepositoryImpl
                    )
                    val getIdsProductsFromBasketUseCase = GetIdsProductsFromBasketUseCase(
                        basketRepository = basketRepositoryImpl,
                        userId = userId
                    )
                    viewModelScope.launch {
                        val resultGetProductsByPath = getProductsByPathUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCTS_BY_PATH,
                                result = result
                            )
                        }

                        val resultGetAllFavorites = getAllFavoritesUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_ALL_FAVORITES,
                                result = result
                            )
                        }

                        val resultGetIdsProductsFromBasket = getIdsProductsFromBasketUseCase.execute().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
                                result = result
                            )
                        }

                        val combinedFlow = combine(
                            resultGetProductsByPath,
                            resultGetAllFavorites,
                            resultGetIdsProductsFromBasket) { productsByPath, allFavorites, idsProductsFromBasket ->

                            return@combine listOf(
                                productsByPath,
                                allFavorites,
                                idsProductsFromBasket
                            )
                        }

                        combinedFlow.collect{ listResults ->
                            listResults.forEach { requestModel ->
                                if (requestModel.result is Result.Error){
                                    _stateScreen.value = requestModel.result
                                    return@collect
                                }
                            }
                            _stateScreen.value = Result.Success(
                                data = listResults
                            )
                        }
                    }

                },
                disconnectionListener = ::onDisconnect
            )
        }
        isShownSendingRequests = false
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun tryAgain(isNetworkStatus: Boolean){
        val mutableListProductsInCatalog = _listProductsInCatalog.value.toMutableList()
        mutableListProductsInCatalog.clear()
        _listProductsInCatalog.value = mutableListProductsInCatalog

        isShownSendingRequests = true
        isShownFillData = true
        isInstallAdapter = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    fun fillData(
        listAllProducts: List<ProductModel>,
        listAllFavorite: List<FavoriteModel>,
        listIdsProductsFromBasket: List<Int>
    ){
        if (isShownFillData) {
            this.listAllProducts = listAllProducts

            val mutableListAllProductsInCatalog = mutableListOf<ProductInCatalogModel>()

            listAllProducts.forEach { productModel ->
                val productId = productModel.productId

                if (userId == UNAUTHORIZED_USER){
                    mutableListAllProductsInCatalog.add(ProductInCatalogModel(
                        isFavorite = false,
                        productModel = productModel,
                        isInBasket = false
                    ))
                }
                else {
                    val isFavorite = listAllFavorite.any { it.productId == productId }
                    val isInBasket = listIdsProductsFromBasket.any { it == productId }

                    mutableListAllProductsInCatalog.add(ProductInCatalogModel(
                        isFavorite = isFavorite,
                        productModel = productModel,
                        isInBasket = isInBasket
                    ))
                }

            }
            listAllProductsInCatalog = mutableListAllProductsInCatalog

            val sortedListProductsInCatalog = sortListProductsInCatalog(listProductsInCatalog = mutableListAllProductsInCatalog)
            _listProductsInCatalog.value = sortedListProductsInCatalog
        }

        isShownFillData = false
    }

    fun listenResultFromProductInfo(
        productId: Int,
        isFavorite: Boolean,
        isInBasket: Boolean
    ){
        setCurrentProductInCatalog(
            productId = productId,
            isFavorite = isFavorite,
            isInBasket = isInBasket
        )

        changeListProductsInCatalog()
    }

    fun listenResultFromFilter(
        isChecked: Boolean,
        priceFrom: Int,
        priceUpTo: Int,
        arrayListIdsSelectedAddresses: ArrayList<Int>?,
        arrayListIdsFiltered: ArrayList<Int>
    ){
        try {
            Log.d("TAG","ProductsViewModel\nisChecked = $isChecked\npriceFrom = $priceFrom\npriceUpTo = $priceUpTo\narrayListIdsSelectedAddresses = $arrayListIdsSelectedAddresses")
            this.isChecked = isChecked
            this.priceFrom = priceFrom
            this.priceUpTo = priceUpTo
            this.arrayListIdsSelectedAddresses = arrayListIdsSelectedAddresses?: throw NullPointerException()

            setListProductsInCatalog(arrayListIdsFiltered = arrayListIdsFiltered)

            checkFilters()

        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun listenResultFromSorting(
        type: Int
    ){
        setIsInstallAdapter(isInstallAdapter = true)
        setListProductsInCatalog(type = type)
    }

    private fun addFavorite(favoriteModel: FavoriteModel) {
        onLoading()
        val addFavoritesUseCase = AddFavoriteUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            favoriteModel = favoriteModel
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            addFavoritesUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }
                val data = listOf(RequestModel(
                    type = TYPE_REMOVE_FAVORITES,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = data)
            }
        }

    }

    private fun removeFavorite(productId: Int) {
        onLoading()

        val deleteByIdUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            deleteByIdUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val data = listOf(RequestModel(
                    type = TYPE_ADD_FAVORITE,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }

    /**
     * Добавление товара в корзину.
     *
     * Параметры:
     * [userId] - идентификатор пользователя в чью корзину будет добавлен товар;
     * [productId] - идентификатор товара;
     * [numberProducts] - количество товара, который будет добавлен. По умолчанию количесто = 1.
     */
    private fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int = 1) {
        onLoading()

        val addProductInBasketUseCase = AddProductInBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            addProductInBasketUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val data = listOf(RequestModel(
                    type = TYPE_ADD_PRODUCT_IN_BASKET,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }

    /**
     * Удаление товара из корзины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей корзины будет удален товар;
     * [productId] - идентификатор товара.
     */
    private fun deleteProductFromBasket(userId: Int, productId: Int) {
        onLoading()

        val deleteProductFromBasketUseCase = DeleteProductFromBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            productId = productId
        )
        viewModelScope.launch {
            delay(MIN_DELAY)

            deleteProductFromBasketUseCase.execute().collect{ result ->
                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                val data = listOf(RequestModel(
                    type = TYPE_DELETE_PRODUCT_FROM_BASKET,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }

    fun installAdapter(block:(Int, Boolean, Int) -> Unit){
        val isEmptyList = _listProductsInCatalog.value.isEmpty()
        val sizeListAllProducts = listAllProducts.size
        if (isInstallAdapter) block(userId,isEmptyList,sizeListAllProducts)

        if (sizeListAllProducts != 0) isInstallAdapter = false
    }

    fun onClickFavorite(
        isNetworkStatus: Boolean,
        favoriteModel: FavoriteModel,
        isFavorite: Boolean,
        navigate: () -> Unit){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                // Если пользователь не авторизован, то открытие экрана авторизации
                if (userId == UNAUTHORIZED_USER) {
                    navigate()
                    return@checkNetworkStatus
                }

                setCurrentProductInCatalog(
                    productId = favoriteModel.productId,
                    isFavorite = isFavorite
                )

                if (isFavorite) {
                    // Добавление
                    addFavorite(favoriteModel = favoriteModel)
                }
                else {
                    // Удаление
                    removeFavorite(productId = favoriteModel.productId)
                }
            },
            disconnectionListener = ::onDisconnect
        )

    }

    fun onClickInBasket(
        isNetworkStatus: Boolean,
        productId:Int,
        isInBasket: Boolean,
        navigate: () -> Unit){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                // Если пользователь не авторизован, то открытие экрана авторизации
                if (userId == UNAUTHORIZED_USER) {
                    navigate()
                    return@checkNetworkStatus
                }

                setCurrentProductInCatalog(
                    productId = productId,
                    isInBasket = isInBasket
                )

                // Добавление/удаление текущего товара из корзины
                if (isInBasket) {
                    addProductInBasket(
                        userId = userId,
                        productId = productId
                    )
                }
                else {
                    deleteProductFromBasket(
                        userId = userId,
                        productId = productId
                    )
                }
            },
            disconnectionListener = ::onDisconnect
        )
    }

    fun navigateToFilters(navigate: (String, Boolean, Int, Int, Int, Int, ArrayList<Int>) -> Unit){
        val listPrices = getListPrices(listAllProducts = listAllProducts)

        defaultPriceFrom = listPrices.min()
        defaultPriceUpTo = listPrices.max()

        navigate(
            path,
            isChecked,
            priceFrom,
            priceUpTo,
            defaultPriceFrom,
            defaultPriceUpTo,
            arrayListIdsSelectedAddresses
        )
    }

    fun navigateToSorting(navigate:(Int) -> Unit){
        navigate(typeSort)
    }

    private fun checkFilters(){
        val isCheckedFilter = if (
            priceFrom == defaultPriceFrom &&
            priceUpTo == defaultPriceUpTo &&
            arrayListIdsSelectedAddresses.size == 0 &&
            !isChecked&&
            _listProductsInCatalog.value.size == listAllProducts.size
        ) false else true

        _isCheckFilter.value = isCheckedFilter
    }

    fun setIsInstallAdapter(isInstallAdapter: Boolean){
        this.isInstallAdapter = isInstallAdapter
    }

    private fun setListProductsInCatalog(arrayListIdsFiltered: ArrayList<Int>) {
        val filteredList = listAllProductsInCatalog.filter { productsInCatalog ->
            arrayListIdsFiltered.contains(productsInCatalog.productModel.productId)
        }
        val sortedListProductsInCatalog = sortListProductsInCatalog(listProductsInCatalog = filteredList)

        _listProductsInCatalog.value = sortedListProductsInCatalog
    }

    private fun setListProductsInCatalog(type: Int) {
        typeSort = type

        val sortedListProductFavorite = sortListProductsInCatalog(listProductsInCatalog = _listProductsInCatalog.value)
        _listProductsInCatalog.value = sortedListProductFavorite
    }

    private fun setCurrentProductInCatalog(productId: Int, isFavorite: Boolean? = null, isInBasket: Boolean? = null){
        try {
            val productInCatalogModel = _listProductsInCatalog.value.find { it.productModel.productId == productId }!!

            val oldIsFavorite = productInCatalogModel.isFavorite
            val oldIsInBasket= productInCatalogModel.isInBasket

            currentProductInCatalog = productInCatalogModel.copy(
                isFavorite = isFavorite?:oldIsFavorite,
                isInBasket = isInBasket?:oldIsInBasket
            )
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun changeListProductsInCatalog(){
        try {
            val mutableListProductsInCatalog = _listProductsInCatalog.value.toMutableList()
            val mutableListAllProductsInCatalog = listAllProductsInCatalog.toMutableList()

            val oldProductInCatalogModel = mutableListProductsInCatalog.find { it.productModel.productId == currentProductInCatalog!!.productModel.productId }
            val index = mutableListProductsInCatalog.indexOf(oldProductInCatalogModel)
            val indexForAllProducts = mutableListAllProductsInCatalog.indexOf(oldProductInCatalogModel)

            if (index >= 0){
                mutableListProductsInCatalog.removeAt(index)
                mutableListProductsInCatalog.add(index,currentProductInCatalog!!)

                _listProductsInCatalog.value = mutableListProductsInCatalog
            }

            if (indexForAllProducts >= 0){
                mutableListAllProductsInCatalog.removeAt(indexForAllProducts)
                mutableListAllProductsInCatalog.add(indexForAllProducts,currentProductInCatalog!!)

                listAllProductsInCatalog = mutableListAllProductsInCatalog
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
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

    private fun sortListProducts(listProducts: List<ProductModel>): List<ProductModel> {
        var sortedListProducts = listOf<ProductModel>()
        try {
            val _sortedListProducts: List<ProductModel> = when (typeSort) {
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

            sortedListProducts = _sortedListProducts
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }

        return sortedListProducts
    }

    private fun sortListProductsInCatalog(listProductsInCatalog: List<ProductInCatalogModel>): List<ProductInCatalogModel> {
        val listProducts = listProductsInCatalog.map { it.productModel }

        val sortedListProducts = sortListProducts(listProducts = listProducts)

        val sortListProductInCatalog = mutableListOf<ProductInCatalogModel>()

        sortedListProducts.forEach { productModel ->
            val productInCatalog = listProductsInCatalog.find { it.productModel == productModel }
            if (productInCatalog != null) sortListProductInCatalog.add(productInCatalog)
        }

        return sortListProductInCatalog
    }

    private fun List<ProductModel>.sortingByDiscountAmount(): List<ProductModel> {
        val sortedListProducts = this.sortedByDescending { it.discount }

        return sortedListProducts
    }


}