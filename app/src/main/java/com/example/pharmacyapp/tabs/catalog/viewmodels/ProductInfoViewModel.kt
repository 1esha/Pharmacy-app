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
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.AddFavoriteUseCase
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.models.ButtonModel
import com.example.domain.models.CurrentButtonModel
import com.example.domain.models.ProductInfoModel
import com.example.domain.models.RequestModel
import com.example.pharmacyapp.CLUB_DISCOUNT
import com.example.pharmacyapp.MIN_DELAY
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_FAVORITE
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_DELETE_PRODUCT_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID
import com.example.pharmacyapp.TYPE_GET_PRODUCT_BY_ID
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.toArrayListInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


/**
 * Класс [ProductInfoViewModel] является viewModel для класса ProductInfoFragment.
 */
class ProductInfoViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl,
    private val basketRepositoryImpl: BasketRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Loading())
    val stateScreen: StateFlow<Result> = _stateScreen

    private var isShownSendingRequests = true

    private var isShownFillData = true

    private var isInit = true

    private val network = Network()

    private val arrayListTitles = arrayListOf<String>()
    private val arrayListBody = arrayListOf<String>()

    private var arrayListIdsAvailabilityPharmacyAddresses = arrayListOf<Int>()

    /**
     * Хранит информацию о текущем товаре.
     */
    private val _productModel = MutableStateFlow<ProductModel?>(null)
    var productModel = _productModel.asStateFlow()

    /**
     * Хранит список наличия текущего товара в аптеках.
     */
    private val _listProductAvailability = MutableStateFlow<List<ProductAvailabilityModel>>(emptyList())
    val listProductAvailability = _listProductAvailability.asStateFlow()

    /**
     * Хранит список идентификаторов товаров из корзины пользователя.
     */
    private val _listIdsProductsFromBasket = MutableStateFlow<List<Int>>(emptyList())
    val listIdsProductsFromBasket = _listIdsProductsFromBasket.asStateFlow()

    private var mutableListIdsProductsFromBasket = mutableListOf<Int>()

    private val _listAllFavorite = MutableStateFlow<List<FavoriteModel>>(emptyList())
    val listAllFavorite = _listAllFavorite

    private var mutableListAllFavorites = mutableListOf<FavoriteModel>()

    private var productId = -1

    private var userId = UNAUTHORIZED_USER

    fun initValues(
        userId: Int,
        productId: Int?
    ){
        try {
            if (isInit) {
                this.userId = userId
                this.productId = productId?: throw NullPointerException()

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

                    viewModelScope.launch {
                        val resultGetProductById = catalogRepositoryImpl.getProductByIdFlow(productId = productId).map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCT_BY_ID,
                                result = result
                            )
                        }

                        val resultGetProductAvailabilityByProductId = catalogRepositoryImpl.getProductAvailabilityByProductIdFlow(productId = productId).map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID,
                                result = result
                            )
                        }

                        val resultGetIdsProductsFromBasket = basketRepositoryImpl.getIdsProductsFromBasketFlow(userId = userId).map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET,
                                result = result
                            )
                        }

                        val resultGetAllFavorites = favoriteRepositoryImpl.getAllFavoritesFlow().map { result ->
                            return@map RequestModel(
                                type = TYPE_GET_ALL_FAVORITES,
                                result = result
                            )
                        }

                        val combinedFlow = combine(
                            resultGetProductById,
                            resultGetProductAvailabilityByProductId,
                            resultGetIdsProductsFromBasket,
                            resultGetAllFavorites) { productById, availabilityByProductId, idsProductsFromBasket, allFavorites ->

                            return@combine listOf(
                                productById,
                                availabilityByProductId,
                                idsProductsFromBasket,
                                allFavorites
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
        isShownSendingRequests = true
        isShownFillData = true
        sendingRequests(isNetworkStatus = isNetworkStatus)
    }

    /**
     * Добавление товара в "Избранное".
     *
     * Параметры:
     * [favoriteModel] - товар, который будет добавлен в "Избранное".
     */
    private fun addFavorite(favoriteModel: FavoriteModel){
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
                    type = TYPE_ADD_FAVORITE,
                    result = result
                ))
                _stateScreen.value = Result.Success(data = data)
            }
        }
    }

    /**
     * Удаление  товара из "Избранного".
     */
    private fun removeFavorite(){
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
                    type = TYPE_REMOVE_FAVORITES,
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
     * [numberProducts] - количество товара, который будет добавлен. По умолчанию количесто = 1.
     */
    private fun addProductInBasket(numberProducts: Int = 1) {
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
     */
    private fun deleteProductFromBasket() {
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

    fun fillData(
        productModel: ProductModel,
        listProductAvailability: List<ProductAvailabilityModel>,
        listIdsProductsFromBasket: List<Int>,
        listAllFavorite: List<FavoriteModel>
    ){
        if (isShownFillData){

            _productModel.value = productModel
            _listProductAvailability.value = listProductAvailability
            _listIdsProductsFromBasket.value = listIdsProductsFromBasket
            _listAllFavorite.value = listAllFavorite

            // Заполнение инструкции по применению данными
            fillingInstructions(list = productModel.productDetailedInfo)
        }

        isShownFillData = false
    }

    fun installMenu(listAllFavorite: List<FavoriteModel>, block: (Boolean) -> Unit){
        if (_stateScreen.value is Result.Success<*>) {
            val isFavorite = listAllFavorite.any { it.productId == productId }
            block(isFavorite)
        }
    }

    fun updateMenu(block: (Boolean) -> Unit){
        if (_stateScreen.value is Result.Success<*>) {
            val isFavorite = _listAllFavorite.value.any { it.productId == productId }
            block(isFavorite)
        }
    }

    fun installProductModel(productModel: ProductModel,block: (ProductInfoModel) -> Unit) {
        with(productModel){

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

            val isDiscount = discount > 0.0

            block(ProductInfoModel(
                image = image,
                title = title,
                textOriginalPrice = textOriginalPrice,
                textDiscount = textDiscount,
                textPrice = textPrice,
                textPriceClub = textPriceClub,
                isDiscount = isDiscount
            ))
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

    fun installProductAvailability(
        listProductAvailability: List<ProductAvailabilityModel>,
        textOutOfStock: String,
        textAvailableIn: String,
        textPharmacy: String,
        textPharmacies: String,
        block: (String) -> Unit
    ){
        // Установка списка идентификаторов аптек с наличием товара для передачи на экран MapFragment
        arrayListIdsAvailabilityPharmacyAddresses = listProductAvailability.filter { productAvailabilityModel ->
            productAvailabilityModel.numberProducts > 0
        }.map { it.addressId }.toArrayListInt()

        // Получаем количевто аптек с количеством товара больше 0
        val numberPharmaciesWithProduct = arrayListIdsAvailabilityPharmacyAddresses.size


        // Получаем строку количества аптек в которых есть выбранный товар.
        // В зависимости от количества аптек меняется текст строки
        val textNumberPharmaciesWithProduct = when(numberPharmaciesWithProduct) {
            0 -> { textOutOfStock }
            1 -> { "$textAvailableIn $numberPharmaciesWithProduct $textPharmacy" }
            else -> {"$textAvailableIn $numberPharmaciesWithProduct $textPharmacies" }
        }

       block(textNumberPharmaciesWithProduct)
    }

    fun installButtonInBasket(listIdsProductsFromBasket: List<Int>,buttonModel: ButtonModel,block: (CurrentButtonModel) -> Unit) = with(buttonModel){
        // Значение находится ли товар в корзине или нет
        val isInBasket = listIdsProductsFromBasket.contains(productId)

        val button = if (isInBasket){
            CurrentButtonModel(
                colorBackground = colorSecondaryContainer,
                colorText = colorOnSecondaryContainer,
                text = textSecondary
            )
        }
        else{
            CurrentButtonModel(
                colorBackground = colorPrimary,
                colorText = colorOnPrimary,
                text = textPrimary
            )
        }

        block(button)
    }
    fun onClickInBasket(isNetworkStatus: Boolean, navigate: () -> Unit){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                // Если пользователь не авторизован, то открывается экране авторизации
                if (userId == UNAUTHORIZED_USER) {
                    navigate()
                    return@checkNetworkStatus
                }

                val isInBasket = _listIdsProductsFromBasket.value.contains(productId)

                mutableListIdsProductsFromBasket =  _listIdsProductsFromBasket.value.toMutableList()
                // Добавление/удаление текущего товара из корзины
                if (isInBasket) {
                    deleteProductFromBasket()
                    mutableListIdsProductsFromBasket.remove(productId)
                }
                else {
                    addProductInBasket()
                    mutableListIdsProductsFromBasket.add(productId)
                }
            },
            disconnectionListener = ::onDisconnect
        )

    }

    fun onClickImageProduct(navigate: (String) -> Unit){
        try {
            navigate(productModel.value!!.image)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun onClickCardInstruction(navigate: (ArrayList<String>,ArrayList<String>) -> Unit){
        navigate(arrayListTitles,arrayListBody)
    }

    fun onClickCardAvailability(navigate: (ArrayList<Int>,ArrayList<Int>) -> Unit){
        try {
            val productId = _productModel.value!!.productId
            val arrayListIdsProducts = arrayListOf(productId)
            val arrayListNumberProducts = arrayListOf(1)
            navigate(arrayListIdsProducts, arrayListNumberProducts)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun onClickFavorite(isNetworkStatus: Boolean, itemId: Int, navigate: () -> Unit){
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                // Если пользователь не авторизован, то открывается экране авторизации
                if (userId == UNAUTHORIZED_USER) {

                    navigate()
                    return@checkNetworkStatus
                }

                mutableListAllFavorites = _listAllFavorite.value.toMutableList()
                // Добавление и удаление из "Избранного"
                when (itemId) {
                    // Удаление
                    R.id.favorite -> {
                        removeFavorite()
                        mutableListAllFavorites.removeIf { it.productId == productId }
                    }
                    // Добавление
                    R.id.favorite_border -> {
                        val favoriteModel = FavoriteModel(
                            productId = productModel.value!!.productId,
                            title = productModel.value!!.title,
                            productPath = productModel.value!!.productPath,
                            price = productModel.value!!.price,
                            discount = productModel.value!!.discount,
                            image = productModel.value!!.image
                        )
                        addFavorite(favoriteModel = favoriteModel)
                        mutableListAllFavorites.add(favoriteModel)
                    }
                }
            },
            disconnectionListener = ::onDisconnect
        )
    }

    fun changeListAllFavorite(){
        _listAllFavorite.value = mutableListAllFavorites
    }

    fun changeListIdsProductsFromBasket(){
        _listIdsProductsFromBasket.value = mutableListIdsProductsFromBasket
    }

    fun onBack(block: (Boolean,Boolean,Int) -> Unit){
        if (_stateScreen.value is Result.Success<*>){
            val isFavorite = _listAllFavorite.value.any { it.productId == productId }
            val isInBasket = _listIdsProductsFromBasket.value.any { it == productId }
            block(isFavorite,isInBasket,productId)
        }
    }

}