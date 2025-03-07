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
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductAvailabilityByProductIdUseCase
import com.example.domain.catalog.usecases.GetProductByIdUseCase
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID
import com.example.pharmacyapp.TYPE_GET_PRODUCT_BY_ID
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Псевдонимы для  resultGetProductAvailabilityByProductId, resultGetProductById.
 */
typealias ResultListProductAvailabilityModel = Result<ResponseValueModel<List<ProductAvailabilityModel>?>>

typealias ResultProductModel = Result<ResponseValueModel<ProductModel?>>

/**
 * Класс [ProductInfoViewModel] является viewModel для класса ProductInfoFragment.
 */
class ProductInfoViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    companion object {
        // Ключи для передачи и получения значения был ли обработан запрос или нет
        private const val KEY_IS_SHOWN_GET_PRODUCT_BY_ID = "KEY_IS_SHOWN_GET_PRODUCT_BY_ID"
        private const val KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID = "KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID"
        private const val KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET = "KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET"
    }

    /**
     * [mediatorProductInfo] - наблюдает за изменениями результатов запросов -
     * [resultGetProductById],
     * [resultGetProductAvailabilityByProductId],
     * [resultAddFavorite],
     * [resultRemoveFavorite],
     * [resultGetIdsProductsFromBasket],
     * [resultAddProductInBasket],
     * [resultDeleteProductFromBasket].
     */
    val mediatorProductInfo = MediatorLiveData<MediatorResultsModel<*>>()

    /**
     * [mediatorProductInfo] - наблюдает за изменениями результатов запросов -
     * [_productModel],
     * [_listProductAvailability],
     * [_listIdsProductsFromBasket].
     */
    val mediatorIsAllRequests = MediatorLiveData<Any?>()

    /**
     * Переменные типа [MutableLiveData], хранящие результаты соответсвующих запросов:
     * [getProductById],
     * [getProductAvailabilityByProductId],
     * [addFavorite],
     * [removeFavorite],
     * [getIdsProductsFromBasket],
     * [addProductInBasket],
     * [deleteProductFromBasket].
     */
    private val resultGetProductById = MutableLiveData<MediatorResultsModel<ResultProductModel>>()

    private val resultGetProductAvailabilityByProductId = MutableLiveData<MediatorResultsModel<ResultListProductAvailabilityModel>>()

    private val resultAddFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultGetIdsProductsFromBasket = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<Int>>>>>()

    private val resultAddProductInBasket = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultDeleteProductFromBasket = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    /**
     * Хранит информацию о текущем товаре.
     */
    private val _productModel = MutableLiveData<ProductModel>()
    val productModel: LiveData<ProductModel> = _productModel

    /**
     * Хранит список наличия текущего товара в аптеках.
     */
    private val _listProductAvailability = MutableLiveData<List<ProductAvailabilityModel>?>()
    val listProductAvailability: LiveData<List<ProductAvailabilityModel>?> = _listProductAvailability

    /**
     * Хранит список идентификаторов товаров из корзины пользователя.
     */
    private val _listIdsProductsFromBasket = MutableLiveData<List<Int>>()
    val listIdsProductsFromBasket: LiveData<List<Int>> = _listIdsProductsFromBasket

    /**
     * Переменные:
     * [isShownGetProductById],
     * [isShownGetProductAvailabilityByProductId],
     * [isShownGetIdsProductsFromBasket],
     * хранят значения соответствующих запросов был ли обработан запрос или нет.
     * По умолчаню запросы не обработаны. Значение - false.
     */
    val isShownGetProductById: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_BY_ID] ?: false

    val isShownGetProductAvailabilityByProductId: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID] ?: false

    val isShownGetIdsProductsFromBasket: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] ?: false

    /**
     * Переменная [errorType] хранит тип ошибки. По умолчанию является - [OtherError].
     */
    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    /**
     * Установка источников наблюдения для mediatorProductInfo и mediatorIsAllRequests при инициализации класса.
     */
    init {

        mediatorProductInfo.addSource(resultGetProductById) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultGetProductAvailabilityByProductId) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultGetIdsProductsFromBasket) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultAddFavorite) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultRemoveFavorite) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultAddProductInBasket) { result ->
            mediatorProductInfo.value = result
        }

        mediatorProductInfo.addSource(resultDeleteProductFromBasket) { result ->
            mediatorProductInfo.value = result
        }
        //
        mediatorIsAllRequests.addSource(_productModel) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_listProductAvailability) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_listIdsProductsFromBasket) { result ->
            mediatorIsAllRequests.value = result
        }

    }

    /**
     * Получение товара по идентификатору товара.
     *
     * Парметры:
     * [productId] -  идентификатор товара.
     */
    fun getProductById(productId: Int) {
        val getProductByIdUseCase = GetProductByIdUseCase(
            catalogRepository = catalogRepositoryImpl,
            productId = productId)

        viewModelScope.launch {
            val result = getProductByIdUseCase.execute()

            resultGetProductById.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_BY_ID,
                result = result
            )
        }
    }

    /**
     * Получение списка наличия товара в аптках по идентификатору товара.
     *
     * Параметры:
     * [productId] - идентификатор товара.
     */
    fun getProductAvailabilityByProductId(productId: Int) {
        val getProductAvailabilityByProductIdUseCase = GetProductAvailabilityByProductIdUseCase(
            catalogRepository = catalogRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            val result = getProductAvailabilityByProductIdUseCase.execute()

            resultGetProductAvailabilityByProductId.value = MediatorResultsModel(
                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID,
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
            delay(MIN_DELAY)
            val resultAddFavorite = addFavoriteUseCase.execute()
            this@ProductInfoViewModel.resultAddFavorite.value = MediatorResultsModel(
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
            delay(MIN_DELAY)
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
            delay(MIN_DELAY)
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
            delay(MIN_DELAY)
            val result = deleteProductFromBasketUseCase.execute()

            resultDeleteProductFromBasket.value = MediatorResultsModel(
                type = TYPE_DELETE_PRODUCT_FROM_BASKET,
                result = result
            )
        }
    }

    /**
     * Установка данных текущего товара.
     *
     * Параметры:
     * [productModel] - информация текущего товара для установки.
     */
    fun setProductModel(productModel: ProductModel) {
        _productModel.value = productModel
    }

    /**
     * Установка списка наличия товара в аптеках.
     *
     * Параметры:
     * [listProductAvailability] - список наличия товара в аптеках для установки.
     */
    fun setListProductAvailability(listProductAvailability: List<ProductAvailabilityModel>) {
        _listProductAvailability.value = listProductAvailability
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
     * Установка результата для [resultGetProductById].
     */
    fun setResultGetProductById(result: ResultProductModel, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductInfoViewModel setResult errorType = null")
        }
        resultGetProductById.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_BY_ID,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetProductAvailabilityByProductId].
     */
    fun setResultGetProductAvailabilityByProductId(result: ResultListProductAvailabilityModel, errorType: ErrorType? = null){
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("ProductInfoViewModel setResult errorType = null")
        }
        resultGetProductAvailabilityByProductId.value = MediatorResultsModel(
            type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID,
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
     * Функции
     * [setIsShownGetProductById],
     * [setIsShownGetProductAvailabilityByProductId],
     * [setIsShownGetIdsProductsFromBasket]
     * устанавливают значения для определения был ли обработан запрос или нет.
     *
     * Параметры:
     * [isShown] - true запрос обработан, false запрос надо обработать.
     */
    fun setIsShownGetProductById(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_BY_ID] = isShown
    }

    fun setIsShownGetProductAvailabilityByProductId(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID] = isShown
    }

    fun setIsShownGetIdsProductsFromBasket(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] = isShown
    }

    /**
     * Отчистка типа ошибки.
     */
    fun clearErrorType() {
        _errorType.value = OtherError()
    }

}