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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.models.FavouriteBasketModel
import com.example.domain.models.RequestModel
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
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.viewmodels.ToolbarViewModel
import com.example.pharmacyapp.tabs.profile.adapters.FavoriteAdapter
import com.example.pharmacyapp.tabs.profile.viewmodels.FavoriteViewModel
import com.example.pharmacyapp.tabs.profile.viewmodels.factories.FavoriteViewModelFactory
import kotlinx.coroutines.launch
import java.lang.Exception

/**
 * Класс [FavoriteFragment] является экраном со списком избранных товаров.
 */
class FavoriteFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!

    private lateinit var navControllerProfile: NavController

    private lateinit var sharedPreferences: SharedPreferences

    private val toolbarViewModel: ToolbarViewModel by activityViewModels()

    private lateinit var favoriteAdapter: FavoriteAdapter

    private val favoriteViewModel: FavoriteViewModel by viewModels(
        factoryProducer = { FavoriteViewModelFactory(context = requireContext()) }
    )

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)

        favoriteViewModel.initValues(
            userId =sharedPreferences.getInt(KEY_USER_ID, UNAUTHORIZED_USER)
        )

        navControllerProfile = findNavController()

        favoriteViewModel.sendingRequests(isNetworkStatus = isNetworkStatus)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                favoriteViewModel.stateScreen.collect{ result ->
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
                favoriteViewModel.listFavouriteBasketModel.collect { listFavouriteBasketModel ->
                    installUI(mutableListFavouriteBasketModel = listFavouriteBasketModel.toMutableList())
                }
            }
        }
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
            favoriteViewModel.tryAgain(isNetworkStatus = isNetworkStatus)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        favoriteViewModel.setIsInstallAdapter(isInstallAdapter = true)
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","FavoriteFragment onSuccessResultListener")
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
                TYPE_GET_ALL_FAVORITES + TYPE_GET_IDS_PRODUCTS_FROM_BASKET -> {
                    Log.i("TAG","fullType = TYPE_GET_ALL_FAVORITES + TYPE_GET_IDS_PRODUCTS_FROM_BASKET")
                    val resultGetAllFavorites = listRequests.find { it.type == TYPE_GET_ALL_FAVORITES }?.result!!.asSuccess()!!
                    val resultGetIdsProductsFromBasket = listRequests.find { it.type == TYPE_GET_IDS_PRODUCTS_FROM_BASKET }?.result!!.asSuccess()!!


                    val responseGetAllFavorites = resultGetAllFavorites.data as ResponseValueModel<*>
                    val responseGetIdsProductsFromBasket = resultGetIdsProductsFromBasket.data as ResponseValueModel<*>

                    val _listAllFavorite = responseGetAllFavorites.value as List<*>
                    val listAllFavorite = _listAllFavorite.map { it as FavoriteModel }

                    val _listIdsProductsFromBasket = responseGetIdsProductsFromBasket.value as List<*>
                    val listIdsProductsFromBasket = _listIdsProductsFromBasket.map { it as Int }

                    favoriteViewModel.fillData(
                        listAllFavorite = listAllFavorite,
                        listIdsProductsFromBasket = listIdsProductsFromBasket
                    )

                }
                TYPE_REMOVE_FAVORITES -> {
                    Log.i("TAG","fullType = TYPE_REMOVE_FAVORITES")
                    favoriteViewModel.removeFromFavorites()
                }
                TYPE_ADD_PRODUCT_IN_BASKET -> {
                    Log.i("TAG","fullType = TYPE_ADD_PRODUCT_IN_BASKET")
                   favoriteViewModel.changeListFavouriteBasketModel()
                }
            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        Log.i("TAG","FavoriteFragment onErrorResultListener")
        val message = getErrorMessage(exception = exception)
        updateUI(flag = FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        Log.i("TAG","FavoriteFragment onLoadingResultListener")
        updateUI(flag = FLAG_PENDING_RESULT)
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

    private fun installUI(mutableListFavouriteBasketModel: MutableList<FavouriteBasketModel>) = with(binding) {
        favoriteViewModel.installUI(listFavouriteBasketModel = mutableListFavouriteBasketModel){ isEmpty ->
            // Если список с товарами пусто, то показать "Список пуст"
            if (isEmpty) {
                tvEmptyListFavorites.visibility = View.VISIBLE
                rvFavorite.visibility = View.GONE
            }
            else {
                tvEmptyListFavorites.visibility = View.GONE
                rvFavorite.visibility = View.VISIBLE

                favoriteViewModel.installAdapter {

                    favoriteAdapter = FavoriteAdapter(
                        mutableListFavouriteBasketModel = mutableListFavouriteBasketModel,
                        deleteFromFavoritesListener = ::onClickDeleteFromFavorites,
                        addInBasketFromFavoritesListener = ::onClickAddInBasketFromFavorites,
                        textCategory = getString(R.string.category)
                    )

                    rvFavorite.adapter = favoriteAdapter
                    rvFavorite.layoutManager = LinearLayoutManager(requireContext())
                }
            }
        }
    }

    private fun onClickDeleteFromFavorites(deletedProductId: Int) {
        favoriteViewModel.onClickDeleteFromFavorites(deletedProductId = deletedProductId)
    }

    private fun onClickAddInBasketFromFavorites(productId: Int) {
        favoriteViewModel.onClickAddInBasketFromFavorites(productId = productId)
    }
}