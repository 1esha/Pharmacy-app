package com.example.pharmacyapp.tabs.profile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.basket.BasketRepositoryImpl
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.basket.usecases.AddProductInBasketUseCase
import com.example.domain.basket.usecases.GetIdsProductsFromBasketUseCase
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Класс [FavoriteViewModel] является viewModel для класса FavoriteFragment.
 */
class FavoriteViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    companion object {
        // Ключи для передачи и получения значения был ли обработан запрос или нет
        private const val KEY_IS_SHOWN_GET_ALL_FAVORITES = "KEY_IS_SHOWN_GET_ALL_FAVORITES"
        private const val KEY_IS_SHOWN_REMOVE_FAVORITES = "KEY_IS_SHOWN_REMOVE_FAVORITES"
        private const val KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET = "KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET"
        // Ключи для передачи и получения значения надо ли устанавливать адаптер
        private const val KEY_IS_SHOWN_FAVORITE_ADAPTER = "KEY_IS_SHOWN_FAVORITE_ADAPTER"
    }

    /**
     * [mediatorFavorites] - наблюдает за изменениями результатов запросов -
     * [resultGetAllFavorites],
     * [resultRemoveFavorite],
     * [resultGetIdsProductsFromBasket],
     * [resultAddProductInBasket].
     */
    val mediatorFavorites = MediatorLiveData<MediatorResultsModel<*>>()

    /**
     * [mediatorIsAllRequests] - наблюдает за изменениями переменных MutableLiveDate -
     * [_listAllFavorite],
     * [_listIdsProductsFromBasket].
     */
    val mediatorIsAllRequests = MediatorLiveData<Any>()

    /**
     * Переменные типа [MutableLiveData], хранящие результаты соответсвующих запросов:
     * [getAllFavorites],
     * [removeFavorite],
     * [getIdsProductsFromBasket],
     * [addProductInBasket].
     */
    private val resultGetAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<FavoriteModel>>>>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val resultGetIdsProductsFromBasket = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<Int>>>>>()

    private val resultAddProductInBasket = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    /**
     * Хранит список всех избранных товаров пользователя.
     */
    private val _listAllFavorite = MutableLiveData<List<FavoriteModel>>()
    val listAllFavorite: LiveData<List<FavoriteModel>> = _listAllFavorite

    /**
     * Хранит список идентификаторов товаров из корзины пользователя.
     */
    private val _listIdsProductsFromBasket = MutableLiveData<List<Int>>()
    val listIdsProductsFromBasket: LiveData<List<Int>> = _listIdsProductsFromBasket

    /**
     * Хранит идентификатор товар, который был добавлен в корзину.
     */
    private val _currentProductIdForAddInBasket = MutableLiveData<Int>()
    val currentProductIdForAddInBasket: LiveData<Int> = _currentProductIdForAddInBasket

    /**
     * Хранит информацию о товаре, который был удален из списка избранного.
     */
    private val _currentFavoriteModelForRemove = MutableLiveData<FavoriteModel>()
    val currentFavoriteModelForRemove: LiveData<FavoriteModel> = _currentFavoriteModelForRemove

    /**
     * Переменные:
     * [isShownGetAllFavorites],
     * [isShownRemoveFavorites],
     * [isShownGetIdsProductsFromBasket],
     * хранят значения соответствующих запросов был ли обработан запрос или нет.
     * По умолчаню запросы не обработаны. Значение - false.
     * Переменная [isShownFavoriteAdapter] - хранит значение надо ли устанавливать адаптер или нет.
     */
    val isShownGetAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] ?: false

    val isShownRemoveFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_REMOVE_FAVORITES] ?: false

    val isShownGetIdsProductsFromBasket: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] ?: false

    val isShownFavoriteAdapter: Boolean get() = savedStateHandle[KEY_IS_SHOWN_FAVORITE_ADAPTER] ?: false

    /**
     * Переменная [errorType] хранит тип ошибки. По умолчанию является - [OtherError].
     */
    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    /**
     * Установка источников наблюдения для mediatorFavorites и mediatorIsAllRequests при инициализации класса.
     */
    init {
        mediatorFavorites.addSource(resultGetAllFavorites) { result ->
            mediatorFavorites.value = result
        }

        mediatorFavorites.addSource(resultRemoveFavorite) { result ->
            mediatorFavorites.value = result
        }

        mediatorFavorites.addSource(resultAddProductInBasket) { result ->
            mediatorFavorites.value = result
        }

        mediatorFavorites.addSource(resultGetIdsProductsFromBasket) { result ->
            mediatorFavorites.value = result
        }
        //
        mediatorIsAllRequests.addSource(_listAllFavorite) { result ->
            mediatorIsAllRequests.value = result
        }

        mediatorIsAllRequests.addSource(_listIdsProductsFromBasket) { result ->
            mediatorIsAllRequests.value = result
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
     * Удаление  товара из "Избранного".
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет удален из "Избранного".
     */
    fun removeFavorite(productId: Int) {
        val removeFavoritesUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            delay(MIN_DELAY)
            val result = removeFavoritesUseCase.execute()

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
    fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int = 1){
        val addProductInBasketUseCase = AddProductInBasketUseCase(
            basketRepository = basketRepositoryImpl,
            userId = userId,
            numberProducts = numberProducts,
            productId = productId
        )

        viewModelScope.launch {
            delay(MIN_DELAY)
            val result = addProductInBasketUseCase.execute()

            resultAddProductInBasket.value = MediatorResultsModel(
                type = TYPE_ADD_PRODUCT_IN_BASKET,
                result = result
            )
        }
    }

    /**
     * Установка списка избранных товаров.
     *
     * Параметры:
     * [listAllFavorite] - список избранного для установки.
     */
    fun setListAllFavorites(listAllFavorite: List<FavoriteModel>) {
        _listAllFavorite.value = listAllFavorite
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
     * Установка идентификатора товара, который будет добавлен в корзину.
     *
     * Параметры:
     * [currentProductIdForAddInBasket] - идентификатор товара для установки.
     */
    fun setCurrentProductIdForAddInBasket(currentProductIdForAddInBasket: Int) {
        _currentProductIdForAddInBasket.value = currentProductIdForAddInBasket
    }

    /**
     * Установка информации товара, который будет удален из списка избранного.
     *
     * Параметры:
     * [currentFavoriteModelForRemove] - информация товара для установки.
     */
    fun setCurrentFavoriteModelForRemove(currentFavoriteModelForRemove: FavoriteModel){
        _currentFavoriteModelForRemove.value = currentFavoriteModelForRemove
    }

    /**
     * Функции
     * [setIsShownGetAllFavorites],
     * [setIsShownRemoveFavorites],
     * [setIsShownGetIdsProductsFromBasket]
     * устанавливают значения для определения был ли обработан запрос или нет.
     *
     * Параметры:
     * [isShown] - true запрос обработан, false запрос надо обработать.
     *
     * Функция [setIsShownFavoriteAdapter] устанавливает значение надо ли устанавливать адаптер или нет.
     *
     * Параметры:
     * [isShown] - true адаптер уже был установлен и заново его устанавливать не надо,
     * false - адаптер не был установлен и его надо установить впервые или его надо установить заново по каким-либо другим причинам.
     */
    fun setIsShownGetAllFavorites(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] = isShown
    }

    fun setIsShownRemoveFavorites(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_REMOVE_FAVORITES] = isShown
    }

    fun setIsShownGetIdsProductsFromBasket(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_IDS_PRODUCTS_FROM_BASKET] = isShown
    }

    fun setIsShownFavoriteAdapter(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_FAVORITE_ADAPTER] = isShown
    }

    /**
     * Установка результата для [resultGetAllFavorites].
     */
    fun setResultGetAllFavorite(result: Result<ResponseValueModel<List<FavoriteModel>>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultGetAllFavorites.value = MediatorResultsModel(
            type = TYPE_GET_ALL_FAVORITES,
            result = result
        )
    }

    /**
     * Установка результата для [resultRemoveFavorite].
     */
    fun setResultRemoveFavorite(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultRemoveFavorite.value = MediatorResultsModel(
            type = TYPE_REMOVE_FAVORITES,
            result = result
        )
    }

    /**
     * Установка результата для [resultAddProductInBasket].
     */
    fun setResultAddProductInBasket(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultAddProductInBasket.value = MediatorResultsModel(
            type = TYPE_ADD_PRODUCT_IN_BASKET,
            result = result
        )
    }

    /**
     * Установка результата для [resultGetIdsProductsFromBasket].
     */
    fun setResultGetIdsProductsFromBasket(result: Result<ResponseValueModel<List<Int>>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultGetIdsProductsFromBasket.value = MediatorResultsModel(
            type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
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