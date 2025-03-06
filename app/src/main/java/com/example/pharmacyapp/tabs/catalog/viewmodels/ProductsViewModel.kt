package com.example.pharmacyapp.tabs.catalog.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.basket.usecases.AddProductInBasketUseCase
import com.example.domain.basket.usecases.DeleteProductFromBasketUseCase
import com.example.domain.basket.usecases.GetIdsProductsFromBasketUseCase
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.catalog.usecases.GetProductsByPathUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_PATH
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Класс [ProductsViewModel] является viewModel для класса ProductsFragment.
 */
class ProductsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    companion object {
        // Ключи для передачи и получения значения был ли обработан запрос или нет
        private const val KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH = "KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH"
        private const val KEY_IS_SHOWN_GET_ALL_FAVORITES = "KEY_IS_SHOWN_GET_ALL_FAVORITES"
        private const val KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET = "KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET"
        private const val KEY_IS_SHOWN_PRODUCTS_ADAPTER = "KEY_IS_SHOWN_PRODUCTS_ADAPTER"
    }

    /**
     * [mediatorProduct] - наблюдает за изменениями результатов запросов -
     * [resultGetProductsByPath],
     * [resultAddFavorite],
     * [resultGetAllFavorites],
     * [resultRemoveFavorite],
     * [resultGetIdsProductsFromBasket],
     * [resultAddProductInBasket],
     * [resultDeleteProductFromBasket].
     */
    val mediatorProduct = MediatorLiveData<MediatorResultsModel<*>>()

    /**
     * [mediatorIsAllRequests] - наблюдает за изменениями переменных MutableLiveDate -
     * [_listProducts],
     * [_listAllFavorites],
     * [_listIdsProductsFromBasket].
     */
    val mediatorIsAllRequests = MediatorLiveData<Any>()

    /**
     * Переменные типа [MutableLiveData], хранящие результаты соответсвующих запросов:
     * [getProductsByPath],
     * [getAllFavorites],
     * [addFavorite],
     * [removeFavorite],
     * [getIdsProductsFromBasket],
     * [addProductInBasket],
     * [deleteProductFromBasket].
     */
    private val resultGetProductsByPath = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<ProductModel>?>>>>()

    private val resultGetAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<FavoriteModel>>>>>()

    private val resultAddFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultGetIdsProductsFromBasket = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<Int>>>>>()

    private val resultAddProductInBasket = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultDeleteProductFromBasket = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    /**
     * Хранит список товаров, отображаемый пользователю.
     * Может изменятся из за фильтрации, сортировки и т.д.
     */
    private val _listProducts = MutableLiveData<List<ProductModel>>(null)
    val listProducts: LiveData<List<ProductModel>> = _listProducts

    /**
     * Хранит список всех товаров, не изменятся.
     * Является начальным значением для списка [_listProducts].
     */
    private val _listAllProducts = MutableLiveData<List<ProductModel>>(null)
    val listAllProducts: LiveData<List<ProductModel>> = _listAllProducts

    /**
     * Хранит список всех избранных товаров пользователя.
     */
    private val _listAllFavorites = MutableLiveData<List<FavoriteModel>>(null)
    val listAllFavorites: LiveData<List<FavoriteModel>> = _listAllFavorites

    /**
     * Хранит список идентификаторов товаров из корзины пользователя.
     */
    private val _listIdsProductsFromBasket = MutableLiveData<List<Int>>()
    val listIdsProductsFromBasket: LiveData<List<Int>> = _listIdsProductsFromBasket

    /**
     * Хранит идентификатор текущего товара для добавления/удаления из корзины.
     */
    private val _currentProductId = MutableLiveData<Int>()
    val currentProductId: LiveData<Int> = _currentProductId

    /**
     * Хранит значени для оображения точки над кнопкой "Фильтры".
     * Значение true - фильтры применены, false - нет.
     */
    private val _isCheckFilter = MutableLiveData<Boolean>(false)
    val isCheckFilter: LiveData<Boolean> = _isCheckFilter

    /**
     * Переменные:
     * [isShownProductsAdapter],
     * [isShownGetProductsByPath],
     * [isShownGetAllFavorites],
     * [isShownGetIdsProductsFromBasket],
     * хранят значения соответствующих запросов был ли обработан запрос или нет.
     * По умолчаню запросы не обработаны. Значение - false.
     */
    val isShownProductsAdapter: Boolean get() = savedStateHandle[KEY_IS_SHOWN_PRODUCTS_ADAPTER] ?: false

    val isShownGetProductsByPath: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] ?: false

    val isShownGetAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] ?: false

    val isShownGetIdsProductsFromBasket: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] ?: false

    /**
     * Переменная [errorType] хранит тип ошибки. По умолчанию является - [OtherError].
     */
    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    /**
     * Установка источников наблюдения для mediatorProduct и mediatorIsAllRequests при инициализации класса.
     */
    init {
        mediatorProduct.addSource(resultGetProductsByPath) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultAddFavorite) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultGetAllFavorites) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultRemoveFavorite) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultGetIdsProductsFromBasket) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultAddProductInBasket) { result ->
            mediatorProduct.value = result
        }

        mediatorProduct.addSource(resultDeleteProductFromBasket) { result ->
            mediatorProduct.value = result
        }
        //
        mediatorIsAllRequests.addSource(_listProducts) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_listAllFavorites) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_listIdsProductsFromBasket) { result ->
            mediatorIsAllRequests.value = result
        }
    }

    /**
     * Получение списка товаров по пути (категории) товара.
     *
     * Параметры:
     * [path] - путь по которому будет получен список товаров.
     */
    fun getProductsByPath(path: String) {
        val getProductsByPathUseCase = GetProductsByPathUseCase(
            catalogRepository = catalogRepositoryImpl,
            path = path
        )

        viewModelScope.launch {
            val result = getProductsByPathUseCase.execute()
            resultGetProductsByPath.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCTS_BY_PATH,
                result = result
            )
        }
    }

    /**
     * Получение списка всех избранных товаров пользователя.
     */
    fun getAllFavorites() {
        val getAllFavoritesUseCase = GetAllFavoritesUseCase(favoriteRepository = favoriteRepositoryImpl)

        viewModelScope.launch {
            val result = getAllFavoritesUseCase.execute()
            resultGetAllFavorites.value = MediatorResultsModel(
                type = TYPE_GET_ALL_FAVORITES,
                result = result
            )
        }
    }

    /**
     * Добавление товара в "Избранное".
     *
     * Параметры:
     * [favoriteModel] - товар, который будет добавлен в "Избранное".
     */
    fun addFavorite(favoriteModel: FavoriteModel)  {
        val addFavoriteUseCase = AddFavoriteUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            favoriteModel = favoriteModel)

        viewModelScope.launch {
            delay(180)
            val resultAddFavorite = addFavoriteUseCase.execute()
            this@ProductsViewModel.resultAddFavorite.value = MediatorResultsModel(
                type = TYPE_ADD_FAVORITE,
                result = resultAddFavorite
            )
        }
    }

    /**
     * Удаление  товара из "Избранного".
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет удален из "Избранного".
     */
    fun removeFavorite(productId: Int) {
        val deleteByIdUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            delay(180)
            val result = deleteByIdUseCase.execute()
            resultRemoveFavorite.value = MediatorResultsModel(
                type = TYPE_REMOVE_FAVORITES,
                result = result
            )
        }

    }

    /**
     * Получение списка идентификаторов товаров из корзины.
     *
     * Параметры:
     * [userId] -  идентификатор пользователя из чьей корзины будет получен список.
     */
    fun getIdsProductsFromBasket(userId: Int) {
        val getIdsProductsFromBasketUseCase = GetIdsProductsFromBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId
        )

        viewModelScope.launch {
            val result = getIdsProductsFromBasketUseCase.execute()

            resultGetIdsProductsFromBasket.value = MediatorResultsModel(
                type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
                result = result
            )
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
    fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int = 1) {
        val addProductsInBasketUseCase = AddProductInBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )

        viewModelScope.launch {
            delay(180)
            val result = addProductsInBasketUseCase.execute()

            resultAddProductInBasket.value = MediatorResultsModel(
                type = TYPE_ADD_PRODUCT_IN_BASKET,
                result = result
            )
        }
    }

    /**
     * Удаление товара из корзины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей корзины будет удален товар;
     * [productId] - идентификатор товара.
     */
    fun deleteProductFromBasket(userId: Int, productId: Int) {
        val deleteProductFromBasketUseCase = DeleteProductFromBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            productId = productId
        )

        viewModelScope.launch {
            delay(180)
            val result = deleteProductFromBasketUseCase.execute()

            resultDeleteProductFromBasket.value = MediatorResultsModel(
                type = TYPE_DELETE_PRODUCT_FROM_BASKET,
                result = result
            )
        }
    }

    /**
     * Изменение списка избранных товаров.
     *
     * Парамектры:
     * [favoriteModel] - товар для добавления/удаления из списка;
     * [isFavorite] - значение в "Избранном" товар или нет.
     */
    fun changeListAllFavorites(favoriteModel: FavoriteModel, isFavorite: Boolean) {
        val listAllFavorites = _listAllFavorites.value ?:
        throw NullPointerException("ProductsViewModel listAllFavorites = null")

        val currentListAllFavorites = mutableListOf<FavoriteModel>()

        listAllFavorites.forEach { currentFavoriteModel->
            currentListAllFavorites.add(currentFavoriteModel)
        }

        if (isFavorite) {
            currentListAllFavorites.add(favoriteModel)
        }
        else {
            currentListAllFavorites.remove(favoriteModel)
        }

        _listAllFavorites.value = currentListAllFavorites
    }

    /**
     * Установка списка избранных товаров.
     *
     * Параметры:
     * [listAllFavorites] - список избранного для установки.
     */
    fun setListAllFavorites(listAllFavorites: List<FavoriteModel>) {
        _listAllFavorites.value = listAllFavorites
    }

    /**
     * Установка списка идентификаторов товаров из корзины
     *
     * Параметры:
     * [listIdsProductsFromBasket] - список идентификаторов товаров для установки.
     */
    fun setListIdsProductsFromBasket(listIdsProductsFromBasket: List<Int>) {
        _listIdsProductsFromBasket.value = listIdsProductsFromBasket
    }

    /**
     * Установка списка товаров для отображения на экарне.
     *
     * Параметры:
     * [listProductModel] - список товаров для установки.
     */
    fun setListProductsModel(listProductModel: List<ProductModel>) {
        _listProducts.value = listProductModel
    }

    /**
     * Установка списка всех товаров по выбранному пути.
     *
     * Параметры:
     * [listProductModel] - список всех товаров для установки.
     */
    fun setListAllProductsModel(listProductModel: List<ProductModel>) {
        _listAllProducts.value = listProductModel
    }

    /**
     * Установка значения включены фильтры или нет.
     *
     * Параметры:
     * [isChecked] -  значение есть фильтры или нет.
     */
    fun setIsCheckFilter(isChecked: Boolean) {
        _isCheckFilter.value = isChecked
    }

    /**
     * Установка идентификатора текущего товара для добавления/удаления из корзины.
     *
     * Параметры:
     * [productId] - идентификатор товара.
     */
    fun setCurrentProductId(productId:Int) {
        _currentProductId.value = productId
    }

    /**
     * Функции
     * [setIsShownGetProductsByPath],
     * [setIsShownGetAllFavorites],
     * [setIsShownGetIdsProductsFromBasket],
     * [setIsShownProductsAdapter]
     * устанавливают значения для определения был ли обработан запрос или нет.
     *
     * Параметры:
     * [isShown] - true запрос обработан, false запрос надо обработать.
     */
    fun setIsShownGetProductsByPath(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCTS_BY_PATH] = isShown
    }

    fun setIsShownGetAllFavorites(isShown: Boolean){
        savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] = isShown
    }

    fun setIsShownGetIdsProductsFromBasket(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] = isShown
    }

    fun setIsShownProductsAdapter(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_PRODUCTS_ADAPTER] = isShown
    }

    /**
     * Установка результата для [resultGetProductsByPath].
     */
    fun setResultGetProductsByPath(result: Result<ResponseValueModel<List<ProductModel>?>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultGetProductsByPath.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCTS_BY_PATH,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetAllFavorites].
     */
    fun setResultGetAllFavorites(result: Result<ResponseValueModel<List<FavoriteModel>>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultGetAllFavorites.value = MediatorResultsModel(
            type = TYPE_GET_ALL_FAVORITES,
            result = result
        )
    }

    /**
     * Установка результата для [resultAddFavorite].
     */
    fun setResultAddFavorite(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultAddFavorite.value = MediatorResultsModel(
            type = TYPE_ADD_FAVORITE,
            result = result
        )
    }

    /**
     * Установка результата для [resultRemoveFavorite].
     */
    fun setResultRemoveFavorites(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultRemoveFavorite.value = MediatorResultsModel(
            type = TYPE_REMOVE_FAVORITES,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetIdsProductsFromBasket].
     */
    fun setResultGetIdsProductsFromBasket(result: Result<ResponseValueModel<List<Int>>>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultGetIdsProductsFromBasket.value = MediatorResultsModel(
            type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
            result = result
        )
    }

    /**
     * Установка результата для [resultAddProductInBasket].
     */
    fun setResultAddProductInBasket(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultAddProductInBasket.value = MediatorResultsModel(
            type = TYPE_ADD_PRODUCT_IN_BASKET,
            result = result
        )
    }

    /**
     * Установка результата для [resultDeleteProductFromBasket].
     */
    fun setResultDeleteProductFromBasket(result: Result<ResponseModel>, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductsViewModel setResult errorType = null")
        }
        resultDeleteProductFromBasket.value = MediatorResultsModel(
            type = TYPE_DELETE_PRODUCT_FROM_BASKET,
            result = result
        )
    }

    /**
     * Отчистка типа ошибки.
     */
    fun clearErrorType() {
        _errorType.value = OtherError()
    }

}