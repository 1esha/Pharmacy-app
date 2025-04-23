package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.search.SearchRepositoryImpl
import com.example.domain.DisconnectionException
import com.example.domain.Network
import com.example.domain.Result
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.usecases.GetProductsBySearchUseCase
import com.example.domain.models.RequestModel
import com.example.domain.search.models.SearchQueryModel
import com.example.domain.search.usecases.AddSearchQueryUseCase
import com.example.domain.search.usecases.DeleteAllSearchQueriesUseCase
import com.example.domain.search.usecases.DeleteSearchQueryUseCase
import com.example.domain.search.usecases.GetAllSearchQueriesUseCase
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.TYPE_CLEAR_ALL_SEARCH_HISTORY
import com.example.pharmacyapp.TYPE_DELETE_SEARCH_QUERY
import com.example.pharmacyapp.TYPE_GET_ALL_SEARCH_QUERIES
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_SEARCH
import com.example.pharmacyapp.TYPE_SAVE_TEXT_SEARCH
import com.example.pharmacyapp.main.SearchFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val catalogRepositoryImpl: CatalogRepositoryImpl,
    private val searchRepositoryImpl: SearchRepositoryImpl
): ViewModel() {

    private val _stateScreen = MutableStateFlow<Result>(Result.Success(data = null))
    val stateScreen: StateFlow<Result> = _stateScreen

    private val network = Network()

    private val _listSearchQueryModel = MutableStateFlow<List<SearchQueryModel>>(emptyList())
    val listSearchQueryModel = _listSearchQueryModel.asStateFlow()

    private val _listProductModel = MutableStateFlow<List<ProductModel>>(emptyList())
    val listProductModel = _listProductModel.asStateFlow()

    private var productId: Int? = null

    private var searchQueryId: Int? = null

    private var searchText = EMPTY_STRING

    private var isShownSendingRequests = true

    private var isInstallSearchHistoryAdapter = true

    fun sendingRequests(){
        try {
            if (isShownSendingRequests) {
                onLoading()

                viewModelScope.launch {
                    GetAllSearchQueriesUseCase(
                        searchRepository = searchRepositoryImpl
                    ).execute().collect { result ->
                        if (result is Result.Error){
                            _stateScreen.value = result
                            return@collect
                        }

                        val listRequest = listOf(
                            RequestModel(
                                type = TYPE_GET_ALL_SEARCH_QUERIES,
                                result = result
                            )
                        )
                        _stateScreen.value = Result.Success(data = listRequest)
                    }
                }
            }
            isShownSendingRequests = false
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun getProducts(
        isNetworkStatus: Boolean,
        searchText: String
    ) {
        network.checkNetworkStatus(
            isNetworkStatus = isNetworkStatus,
            connectionListener = {
                onLoading()

                val getProductsBySearchUseCase = GetProductsBySearchUseCase(
                    catalogRepository = catalogRepositoryImpl,
                    searchText = searchText
                )

                viewModelScope.launch {
                    getProductsBySearchUseCase.execute().collect { result ->
                        if (result is Result.Error){
                            _stateScreen.value = result
                            return@collect
                        }

                        val listRequest = listOf(
                            RequestModel(
                                type = TYPE_GET_PRODUCTS_BY_SEARCH,
                                result = result
                            )
                        )
                        _stateScreen.value = Result.Success(data = listRequest)
                    }
                }
            },
            disconnectionListener = ::onDisconnect
        )
    }

    private fun onLoading(){
        _stateScreen.value = Result.Loading()
    }

    private fun onDisconnect(){
        _stateScreen.value = Result.Error(exception = DisconnectionException())
    }

    fun onDestroyView(){
        isInstallSearchHistoryAdapter = true
    }

    fun tryAgain(isNetworkStatus: Boolean,searchText: String) {
        isShownSendingRequests = true
        isInstallSearchHistoryAdapter = true
        getProducts(isNetworkStatus = isNetworkStatus, searchText = searchText)
    }

    fun fillListProductModel(listProductModel: List<ProductModel>) {
        _listProductModel.value = listProductModel
    }

    fun fillListSearchQueryModel(listSearchQueryModel: List<SearchQueryModel>){
        _listSearchQueryModel.value = listSearchQueryModel
    }

    fun clearList(){
        _listProductModel.value = emptyList()
    }

    fun onClickProduct(productId: Int,searchText: String){
        this.productId = productId

        if (_listSearchQueryModel.value.any { it.searchText == searchText } || searchText == EMPTY_STRING){
            _stateScreen.value = Result.Success(
                data = listOf(
                    RequestModel(
                        type = TYPE_SAVE_TEXT_SEARCH + SearchFragment.NAVIGATE_TO_PRODUCT_INFO,
                        result = Result.Success(data = null)
                    )
                )
            )
            return
        }

        saveTextSearch(searchText = searchText, path = SearchFragment.NAVIGATE_TO_PRODUCT_INFO)
    }

    fun onClickEnter(searchText: String){
        this.searchText = searchText

        if (_listSearchQueryModel.value.any { it.searchText == searchText } || searchText == EMPTY_STRING){
            _stateScreen.value = Result.Success(
                data = listOf(
                    RequestModel(
                        type = TYPE_SAVE_TEXT_SEARCH + SearchFragment.NAVIGATE_TO_PRODUCTS,
                        result = Result.Success(data = null)
                    )
                )
            )
            return
        }

        saveTextSearch(searchText = searchText,path = SearchFragment.NAVIGATE_TO_PRODUCTS)
    }

    fun navigateToProductInfo(block: (Int) -> Unit){
        try {
            block(productId!!)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun navigateToProducts(block: (String) -> Unit){
        try {
            block(searchText)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    private fun saveTextSearch(searchText: String,path: String) = viewModelScope.launch {
        try {
            onLoading()

            AddSearchQueryUseCase(
                searchRepository = searchRepositoryImpl,
                searchText = searchText
            ).execute().collect { result ->

                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                _stateScreen.value = Result.Success(
                    data = listOf(
                        RequestModel(
                            type = TYPE_SAVE_TEXT_SEARCH + path,
                            result = result
                        )
                    )
                )
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun deleteSearchQuery(searchQueryId: Int) {
        viewModelScope.launch {
            try {
                onLoading()

                DeleteSearchQueryUseCase(
                    searchRepository = searchRepositoryImpl,
                    searchQueryId = searchQueryId
                ).execute().collect { result ->
                    if (result is Result.Error){
                        _stateScreen.value = result
                        return@collect
                    }

                    this@SearchViewModel.searchQueryId = searchQueryId

                    _stateScreen.value = Result.Success(
                        data = listOf(
                            RequestModel(
                                type = TYPE_DELETE_SEARCH_QUERY,
                                result = result
                            )
                        )
                    )
                }
            }
            catch (e: Exception){
                Log.e("TAG",e.stackTraceToString())
                _stateScreen.value = Result.Error(exception = e)
            }
        }
    }

    fun onClearAllHistory(){
        isInstallSearchHistoryAdapter = true

        _listSearchQueryModel.value = emptyList()
    }

    fun clearAllHistory() = viewModelScope.launch {
        try {
            DeleteAllSearchQueriesUseCase(
                searchRepository = searchRepositoryImpl
            ).execute().collect { result ->

                if (result is Result.Error){
                    _stateScreen.value = result
                    return@collect
                }

                _stateScreen.value = Result.Success(
                    data = listOf(
                        RequestModel(
                            type = TYPE_CLEAR_ALL_SEARCH_HISTORY,
                            result = result
                        )
                    )
                )
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
            _stateScreen.value = Result.Error(exception = e)
        }
    }

    fun installSearchHistoryAdapter(block: () -> Unit){
        if (isInstallSearchHistoryAdapter) {
            block()
        }
        if (_listSearchQueryModel.value.isNotEmpty()) isInstallSearchHistoryAdapter = false
    }

    fun installSearchAdapter(
        listProductModel: List<ProductModel>,
        block: (Boolean,Boolean) -> Unit
    ){
        val isVisibleSearch = listProductModel.isNotEmpty()
        val isVisibleSearchHistory = listProductModel.isEmpty() && _listSearchQueryModel.value.isNotEmpty()

        block(isVisibleSearch,isVisibleSearchHistory)
    }

    fun onDeleteSearchQuery(){
        val mutableListSearchQueryModel = _listSearchQueryModel.value.toMutableList()

        val searchQueryModel = _listSearchQueryModel.value.find { it.searchQueryId == this.searchQueryId }

        mutableListSearchQueryModel.remove(searchQueryModel)

        isInstallSearchHistoryAdapter = true
        _listSearchQueryModel.value = mutableListSearchQueryModel
    }

}