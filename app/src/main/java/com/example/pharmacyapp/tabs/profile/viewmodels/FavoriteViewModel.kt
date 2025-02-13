package com.example.pharmacyapp.tabs.profile.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.Result
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.DeleteByIdUseCase
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.models.MediatorResultsModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.pharmacyapp.TYPE_GET_ALL_FAVORITES
import com.example.pharmacyapp.TYPE_REMOVE_FAVORITES
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_ALL_FAVORITES = "KEY_IS_SHOWN_GET_ALL_FAVORITES"
        const val KEY_IS_SHOWN_REMOVE_FAVORITES = "KEY_IS_SHOWN_REMOVE_FAVORITES"
    }

    val mediatorFavorites = MediatorLiveData<MediatorResultsModel<*>>()

    private val resultGetAllFavorites = MutableLiveData<MediatorResultsModel<Result<ResponseValueModel<List<FavoriteModel>>>>>()

    private val resultRemoveFavorite = MutableLiveData<MediatorResultsModel<Result<ResponseModel>>>()

    private val _listAllFavorite = MutableLiveData<List<*>>()
    val listAllFavorite: LiveData<List<*>> = _listAllFavorite

    val isShownGetAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] ?: false
    val isShownRemoveFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_REMOVE_FAVORITES] ?: false

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    init {
        mediatorFavorites.addSource(resultGetAllFavorites) { result ->
            mediatorFavorites.value = result
        }

        mediatorFavorites.addSource(resultRemoveFavorite) { result ->
            mediatorFavorites.value = result
        }
    }

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

    fun removeFavorite(productId: Int) {
        val removeFavoritesUseCase = DeleteByIdUseCase(
            favoriteRepository = favoriteRepositoryImpl,
            productId = productId
        )

        viewModelScope.launch {
            val result = removeFavoritesUseCase.execute()

            resultRemoveFavorite.value = MediatorResultsModel(
                type = TYPE_REMOVE_FAVORITES,
                result = result
            )
        }
    }

    fun setListAllFavorites(list: List<*>) {
        _listAllFavorite.value = list
    }

    fun setResultGetAllFavorite(result: Result<ResponseValueModel<List<FavoriteModel>>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultGetAllFavorites.value = MediatorResultsModel(
            type = TYPE_GET_ALL_FAVORITES,
            result = result
        )
    }

    fun setResultRemoveFavorite(result: Result<ResponseModel>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        resultRemoveFavorite.value = MediatorResultsModel(
            type = TYPE_REMOVE_FAVORITES,
            result = result
        )
    }

    fun setIsShownGetAllFavorites(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] = isShown
    }

    fun setIsShownRemoveFavorites(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_REMOVE_FAVORITES] = isShown
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

}