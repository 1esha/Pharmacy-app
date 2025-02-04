package com.example.pharmacyapp.tabs.profile.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.favorite.FavoriteRepositoryImpl
import com.example.domain.ErrorResult
import com.example.domain.ErrorType
import com.example.domain.OtherError
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.favorite.usecases.GetAllFavoritesUseCase
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.launch

class FavoriteViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val favoriteRepositoryImpl: FavoriteRepositoryImpl
): ViewModel() {

    companion object {
        const val KEY_IS_SHOWN_GET_ALL_FAVORITES = "KEY_IS_SHOWN_GET_ALL_FAVORITES"
    }

    private val _resultGetAllFavorites = MutableLiveData<Result<ResponseValueModel<List<FavoriteModel>>>>(PendingResult())
    val resultGetAllFavorites: LiveData<Result<ResponseValueModel<List<FavoriteModel>>>> = _resultGetAllFavorites

    private val _listAllFavorite = MutableLiveData<List<*>>()
    val listAllFavorite: LiveData<List<*>> = _listAllFavorite

    val isShownGetAllFavorites: Boolean get() = savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] ?: false

    private val _errorType = MutableLiveData<ErrorType>(OtherError())
    val errorType: LiveData<ErrorType> = _errorType

    fun getAllFavorites() {
        val getAllFavoritesUseCase = GetAllFavoritesUseCase(favoriteRepository = favoriteRepositoryImpl)

        viewModelScope.launch {
            val result = getAllFavoritesUseCase.execute()

            _resultGetAllFavorites.value = result
        }
    }

    fun setListAllFavorites(list: List<*>) {
        _listAllFavorite.value = list
    }

    fun setResultGetAllFavorite(result: Result<ResponseValueModel<List<FavoriteModel>>>, errorType: ErrorType? = null) {
        if (result is ErrorResult && errorType != null){
            _errorType.value = errorType?: throw NullPointerException("FavoriteViewModel setResult errorType = null")
        }
        _resultGetAllFavorites.value = result
    }

    fun setIsShownGetAllFavorites(isShown: Boolean) {
        savedStateHandle[KEY_IS_SHOWN_GET_ALL_FAVORITES] = isShown
    }

    fun clearErrorType() {
        _errorType.value = OtherError()
    }

}