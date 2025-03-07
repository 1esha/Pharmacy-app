package com.example.pharmacyapp.tabs.profile

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
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.DisconnectionError
import com.example.domain.ErrorResult
import com.example.domain.Network
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.models.FavouriteBasketModel
import com.example.domain.profile.ProfileResult
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_USER_ID
import com.example.pharmacyapp.NAME_SHARED_PREFERENCES
import com.example.pharmacyapp.R
import com.example.pharmacyapp.TYPE_ADD_PRODUCT_IN_BASKET
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_GET_IDS_PRODUCTS_FROM_BASKET
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import com.example.pharmacyapp.ToolbarSettingsModel
import com.example.pharmacyapp.UNAUTHORIZED_USER
import com.example.pharmacyapp.databinding.FragmentFavoriteBinding
import com.example.pharmacyapp.getMessageByErrorType
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.adapters.FavoriteAdapter
import com.example.pharmacyapp.tabs.profile.viewmodels.FavoriteViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.FavoriteViewModelFactory
import java.lang.Exception
import kotlin.properties.Delegates

/**
 * Класс [FavoriteFragment] является экраном со списком избранных товаров.
 */
class FavoriteFragment : Fragment(), ProfileResult {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var navControllerProfile: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private var userId by Delegates.notNull<Int>()

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private val favoriteViewModel: FavoriteViewModel by viewModels(
        factoryProducer = { FavoriteViewModelFactory(context = requireContext()) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        // Получение id пользователя
        userId = sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)

        navControllerProfile = findNavController()

        // Отправка необходимых запросов
        sendingRequests()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding){

        // Установка toolbar
        toolbarViewModel.installToolbar(toolbarSettingsModel = ToolbarSettingsModel(
            title = getString(R.string.favourites),
            icon = R.drawable.ic_back
        ) {
            navControllerProfile.navigateUp()
        })
        // Установка меню
        toolbarViewModel.clearMenu()

        // Обработка кнопки "Прпробовать снова"
        layoutPendingResultFavorite.bTryAgain.setOnClickListener {
            // Установка значение "не показано" для запросов
            favoriteViewModel.setIsShownFavoriteAdapter(isShown = false)
            favoriteViewModel.setIsShownGetAllFavorites(isShown = false)
            favoriteViewModel.setIsShownGetIdsProductsFromBasket(isShown = false)

            // Повторный вызов всех запросов
            sendingRequests()
        }

        // Наблюдение за изменениями listAllFavorite, listIdsProductsFromBasket
        favoriteViewModel.mediatorIsAllRequests.observe(viewLifecycleOwner) {
            val listAllFavorite = favoriteViewModel.listAllFavorite.value
            val listIdsProductsFromBasket = favoriteViewModel.listIdsProductsFromBasket.value

            // Если результаты по всем запросам пришли
            if (
                listAllFavorite != null &&
                listIdsProductsFromBasket != null
                ) {

                // Установка списка избранного
                installListFavourites(
                    listAllFavorite = listAllFavorite,
                    listIdsProductsFromBasket = listIdsProductsFromBasket
                )

                updateUI(flag = FLAG_SUCCESS_RESULT)
            }
        }

        // Наблюдение за получением результатов запросов
        favoriteViewModel.mediatorFavorites.observe(viewLifecycleOwner) { mediatorResult ->
            val type = mediatorResult.type
            val result = mediatorResult.result as Result<*>

            when(result){
                is PendingResult -> { onPendingResultListener() }
                is SuccessResult -> {
                    onSuccessResultListener(
                        userId = userId,
                        value = result.value,
                        type = type)
                }
                is ErrorResult -> {
                    val errorType = favoriteViewModel.errorType.value
                    val message = getString(getMessageByErrorType(errorType = errorType))
                    onErrorResultListener(exception = result.exception, message = message)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        favoriteViewModel.setIsShownFavoriteAdapter(isShown = false)
    }

    override fun <T> onSuccessResultListener(userId: Int, value: T, type: String?) {

        when(type) {
            TYPE_GET_ALL_FAVORITES -> {
                Log.i("TAG","FavoriteFragment onSuccessResultListener TYPE_GET_ALL_FAVORITES")
                val isShownGetAllFavorites = favoriteViewModel.isShownGetAllFavorites

                if (!isShownGetAllFavorites) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val responseModel = responseValueModel.responseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {

                        val _listAllFavorites = responseValueModel.value as List<*>
                        val listAllFavorites = _listAllFavorites.map {
                            return@map it as FavoriteModel
                        }

                        favoriteViewModel.setListAllFavorites(listAllFavorite = listAllFavorites)

                    }
                    else {
                        favoriteViewModel.setResultGetAllFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                favoriteViewModel.setIsShownGetAllFavorites(isShown = true)
            }

            TYPE_REMOVE_FAVORITES -> {
                Log.i("TAG","FavoriteFragment onSuccessResultListener TYPE_REMOVE_FAVORITES")
                val isShownRemoveFavorites = favoriteViewModel.isShownRemoveFavorites

                if (!isShownRemoveFavorites) {
                    val responseModel = value as ResponseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {

                        val listAllFavorite = favoriteViewModel.listAllFavorite.value ?:
                        throw NullPointerException("FavoriteFragment listAllFavorite = null")

                        val currentFavoriteModel = favoriteViewModel.currentFavoriteModelForRemove.value

                        if (currentFavoriteModel != null) {

                            val mutableListAllFavorite = listAllFavorite.toMutableList()

                            mutableListAllFavorite.remove(currentFavoriteModel)

                            favoriteViewModel.setListAllFavorites(listAllFavorite = mutableListAllFavorite)

                            getSupportActivity().showToast(getString(R.string.the_product_was_removed_from_the_favorites_section))
                        }

                    }
                    else {
                        favoriteViewModel.setResultRemoveFavorite(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                favoriteViewModel.setIsShownRemoveFavorites(isShown = true)
            }
            TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> {
                Log.i("TAG","FavoriteFragment onSuccessResultListener TYPE_GET_IDS_PRODUCTS_FROM_BASKET")
                val isShownGetIdsProductsFromBasket = favoriteViewModel.isShownGetIdsProductsFromBasket

                if (!isShownGetIdsProductsFromBasket) {
                    val responseValueModel = value as ResponseValueModel<*>
                    val responseModel = responseValueModel.responseModel
                    val status = responseModel.status
                    val message = responseModel.message

                    if (status in 200..299) {

                        val _listIdsProductsFromBasket = responseValueModel.value as List<*>
                        val listIdsProductsFromBasket = _listIdsProductsFromBasket.map {
                            return@map it as Int
                        }

                        favoriteViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = listIdsProductsFromBasket)

                    }
                    else {
                        favoriteViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                        if (message != null) getSupportActivity().showToast(message = message)
                    }
                }

                favoriteViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
            }
            TYPE_ADD_PRODUCT_IN_BASKET -> {

                val responseModel = value as ResponseModel
                val status = responseModel.status
                val message = responseModel.message

                if (status in 200..299) {

                    val listIdsProductsFromBasket = favoriteViewModel.listIdsProductsFromBasket.value ?:
                    throw NullPointerException("FavoriteFragment listIdsProductsFromBasket = null")
                    val currentProductId = favoriteViewModel.currentProductIdForAddInBasket.value

                    if (currentProductId != null) {

                        val mutableListIdsProductsFromBasket = listIdsProductsFromBasket.toMutableList()

                        mutableListIdsProductsFromBasket.add(currentProductId)

                        favoriteViewModel.setListIdsProductsFromBasket(listIdsProductsFromBasket = mutableListIdsProductsFromBasket)
                    }

                }
                else {
                    favoriteViewModel.setResultAddProductInBasket(result = ErrorResult(exception = Exception()), errorType = OtherError())
                    if (message != null) getSupportActivity().showToast(message = message)
                }
            }
        }

    }

    override fun onErrorResultListener(exception: Exception, message: String) {
        Log.i("TAG","FavoriteFragment onErrorResultListener")
        // При ошибке установка знчений "показано" для запросов, чтобы запросы не отправлялись заново
        favoriteViewModel.setIsShownGetAllFavorites(isShown = true)
        favoriteViewModel.setIsShownGetIdsProductsFromBasket(isShown = true)
        favoriteViewModel.setIsShownRemoveFavorites(isShown = true)
        favoriteViewModel.setIsShownFavoriteAdapter(isShown = true)

        updateUI(flag = FLAG_ERROR_RESULT, messageError = message)
    }

    override fun onPendingResultListener() {
        Log.i("TAG","FavoriteFragment onPendingResultListener")
        // Отчистка типа ошибки
        favoriteViewModel.clearErrorType()

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
                    TYPE_GET_ALL_FAVORITES -> favoriteViewModel.setResultGetAllFavorite(result = PendingResult())
                    TYPE_REMOVE_FAVORITES -> favoriteViewModel.setResultRemoveFavorite(result = PendingResult())
                    TYPE_ADD_PRODUCT_IN_BASKET -> favoriteViewModel.setResultAddProductInBasket(result = PendingResult())
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> favoriteViewModel.setResultGetIdsProductsFromBasket(result = PendingResult())
                }

                onSuccessfulEventListener()
            },
            disconnectionListener = {
                val currentException = if (exception == null) Exception() else  exception
                val errorType = DisconnectionError()

                when(type) {
                    TYPE_GET_ALL_FAVORITES -> favoriteViewModel.setResultGetAllFavorite(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_REMOVE_FAVORITES -> favoriteViewModel.setResultRemoveFavorite(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_ADD_PRODUCT_IN_BASKET -> favoriteViewModel.setResultAddProductInBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                    TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> favoriteViewModel.setResultGetIdsProductsFromBasket(result = ErrorResult(exception = currentException), errorType = errorType)
                }

            }
        )
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultFavorite){
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
     * Отправка запросов для получения необходимых данных
     */
    private fun sendingRequests(){

        val isShownGetAllFavorites = favoriteViewModel.isShownGetAllFavorites
        val isShownGetIdsProductsFromBasket = favoriteViewModel.isShownGetIdsProductsFromBasket

        // Если запросы "не показаны", то отправить запросы
        if (!isShownGetAllFavorites) {
            onSuccessfulEvent(type = TYPE_GET_ALL_FAVORITES) {
                favoriteViewModel.getAllFavorites()
            }
        }

        if (!isShownGetIdsProductsFromBasket) {
            onSuccessfulEvent(type = TYPE_GET_IDS_PRODUCTS_FROM_BASKET) {
                favoriteViewModel.getIdsProductsFromBasket(userId = userId)
            }
        }
    }

    /**
     * Установка списка избранных товаров для экрана.
     *
     * Параметры:
     * [listAllFavorite] - список всех избранных товаров;
     * [listIdsProductsFromBasket] - список идентификаторов товаров из корзины.
     */
    private fun installListFavourites(listAllFavorite: List<FavoriteModel>,listIdsProductsFromBasket: List<Int>) = with(binding) {

        // Если список с товарами пусто, то показать "Список пуст"
        if (listAllFavorite.isEmpty()) {
            tvEmptyListFavorites.visibility = View.VISIBLE
        }
        else {
            tvEmptyListFavorites.visibility = View.GONE

            val mutableListFavoriteBasket = mutableListOf<FavouriteBasketModel>()

            // Заполнение и установка списка для вывода на экран
            listAllFavorite.forEach { favoriteModel ->
                val isInBasket = listIdsProductsFromBasket.any { it == favoriteModel.productId }

                mutableListFavoriteBasket.add(
                    FavouriteBasketModel(
                        favoriteModel = favoriteModel,
                        isInBasket = isInBasket
                    )
                )
            }

            val favoriteAdapter = FavoriteAdapter(
                listItems = mutableListFavoriteBasket,
                deleteFromFavoritesListener = ::onClickDeleteFromFavorites,
                addInBasketFromFavoritesListener = ::onClickAddInBasketFromFavorites,
                textCategory = getString(R.string.category)
            )

            val isShownFavoriteAdapter = favoriteViewModel.isShownFavoriteAdapter

            // Если адаптер не установлен, то установить
            if (!isShownFavoriteAdapter) {
                rvFavorite.adapter = favoriteAdapter
                rvFavorite.layoutManager = LinearLayoutManager(requireContext())
                favoriteViewModel.setIsShownFavoriteAdapter(isShown = true)
            }

        }

    }

    /**
     * Обработка удаления из списка избранных товаров.
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет удален;
     * [favoriteModel] - данные товара, который будет удален.
     */
    private fun onClickDeleteFromFavorites(productId: Int, favoriteModel: FavoriteModel) {
        onSuccessfulEvent(type = TYPE_REMOVE_FAVORITES) {
            // Установка значения "не показано", чтобы запрос был обработан
            favoriteViewModel.setIsShownRemoveFavorites(isShown = false)
            // Отправка запроса на удаление товара
            favoriteViewModel.removeFavorite(productId = productId)
        }
        // Установка данных товара, который будет удален. Для получения этих данных в обработке запроса
        favoriteViewModel.setCurrentFavoriteModelForRemove(currentFavoriteModelForRemove = favoriteModel)
    }

    /**
     * Обработка добавления товара в корзину
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет добавлен в корзину.
     */
    private fun onClickAddInBasketFromFavorites(productId: Int) {
        // Отправка запроса на добавление в корзину
        onSuccessfulEvent(type = TYPE_ADD_PRODUCT_IN_BASKET){
            favoriteViewModel.addProductInBasket(userId = userId,productId = productId)
        }
        // Установка идентификатора товара, который будет добавлен в корзину. Для получения этого идентификатора в обработке запроса
        favoriteViewModel.setCurrentProductIdForAddInBasket(currentProductIdForAddInBasket = productId)
    }
}