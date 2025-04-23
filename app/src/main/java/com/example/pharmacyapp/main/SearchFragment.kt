package com.example.pharmacyapp.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Result
import com.example.domain.ResultProcessing
import com.example.domain.asSuccess
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.RequestModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.search.models.SearchQueryModel
import com.example.pharmacyapp.EMPTY_STRING
import com.example.pharmacyapp.FLAG_ERROR_RESULT
import com.example.pharmacyapp.FLAG_PENDING_RESULT
import com.example.pharmacyapp.FLAG_PRODUCTS_BY_SEARCH
import com.example.pharmacyapp.FLAG_SUCCESS_RESULT
import com.example.pharmacyapp.KEY_FLAGS_FOR_PRODUCTS
import com.example.pharmacyapp.KEY_PRODUCT_ID
import com.example.pharmacyapp.KEY_SEARCH_TEXT
import com.example.pharmacyapp.TYPE_CLEAR_ALL_SEARCH_HISTORY
import com.example.pharmacyapp.TYPE_DELETE_SEARCH_QUERY
import com.example.pharmacyapp.TYPE_GET_ALL_SEARCH_QUERIES
import com.example.pharmacyapp.TYPE_GET_PRODUCTS_BY_SEARCH
import com.example.pharmacyapp.TYPE_SAVE_TEXT_SEARCH
import com.example.pharmacyapp.databinding.FragmentSearchBinding
import com.example.pharmacyapp.getErrorMessage
import com.example.pharmacyapp.getSupportActivity
import com.example.pharmacyapp.main.adapters.SearchAdapter
import com.example.pharmacyapp.main.adapters.SearchHistoryAdapter
import com.example.pharmacyapp.main.viewmodels.SearchViewModel
import com.example.pharmacyapp.main.viewmodels.factories.SearchViewModelFactory
import com.example.pharmacyapp.tabs.catalog.CatalogFragment
import com.google.android.material.search.SearchView
import kotlinx.coroutines.launch

class SearchFragment : Fragment(), ResultProcessing {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels(
        factoryProducer = { SearchViewModelFactory(context = requireContext()) }
    )

    private lateinit var searchAdapter: SearchAdapter

    private lateinit var searchHistoryAdapter: SearchHistoryAdapter

    private val isNetworkStatus get() =  getSupportActivity().isNetworkStatus(context = requireContext())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        searchViewModel.sendingRequests()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                searchViewModel.stateScreen.collect{ result ->
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
                searchViewModel.listSearchQueryModel.collect{ listSearchQueryModel ->
                    installSearchHistoryAdapter(listSearchQueryModel = listSearchQueryModel)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                searchViewModel.listProductModel.collect{ listProductModel ->
                    installSearchAdapter(listProductModel = listProductModel)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {

        searchView.show()

        searchView.addTransitionListener { searchView, previousState, newState ->
            if (newState == SearchView.TransitionState.HIDING) {
                findNavController().navigateUp()
            }
        }

        searchView.editText.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == KeyEvent.ACTION_DOWN){
                if (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER){
                    val searchText = searchView.editText.text.toString()
                    searchViewModel.onClickEnter(searchText = searchText)
                }
            }

            false
        }

        searchView.editText.addTextChangedListener(
            object : TextWatcher {
                override fun beforeTextChanged(search: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(search: CharSequence?, start: Int, before: Int, count: Int) {
                    if (count > 0){
                        searchViewModel.getProducts(
                            isNetworkStatus = isNetworkStatus,
                            searchText = search.toString()
                        )
                    }
                    else {
                        searchViewModel.clearList()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {}
            }
        )

        layoutPendingResultSearch.bTryAgain.setOnClickListener {
            val searchText = searchView.editText.text.toString()
            searchViewModel.tryAgain(isNetworkStatus = isNetworkStatus, searchText = searchText)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchViewModel.onDestroyView()
        _binding = null
    }

    override fun <T> onSuccessResultListener(data: T) {
        Log.i("TAG","SearchFragment onSuccessResultListener")
        try {
            if (data == null){
                updateUI(flag = FLAG_SUCCESS_RESULT)
                return
            }

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
                TYPE_GET_PRODUCTS_BY_SEARCH -> {
                    val resultGetProductsBySearch = listRequests.find { it.type == TYPE_GET_PRODUCTS_BY_SEARCH }?.result?.asSuccess()!!

                    val responseGetProductsBySearch = resultGetProductsBySearch.data as ResponseValueModel<*>

                    val _listProductModel = responseGetProductsBySearch.value as List<*>
                    val listProductModel = _listProductModel.map { it as ProductModel }

                    searchViewModel.fillListProductModel(
                        listProductModel = listProductModel
                    )

                }
                TYPE_SAVE_TEXT_SEARCH + NAVIGATE_TO_PRODUCT_INFO-> {
                    searchViewModel.navigateToProductInfo { productId ->

                        getSupportActivity().setFragmentResult(
                            CatalogFragment.KEY_RESULT_FROM_SEARCH_PRODUCT_ID,
                            bundleOf(KEY_PRODUCT_ID to productId)
                        )

                        binding.searchView.hide()
                    }

                }
                TYPE_SAVE_TEXT_SEARCH + NAVIGATE_TO_PRODUCTS -> {
                    searchViewModel.navigateToProducts { _searchText ->

                        var searchText: String? = _searchText

                        if (searchText == EMPTY_STRING) searchText = null

                        getSupportActivity().setFragmentResult(
                            CatalogFragment.KEY_RESULT_FROM_SEARCH_PRODUCTS,
                            bundleOf(
                                KEY_FLAGS_FOR_PRODUCTS to FLAG_PRODUCTS_BY_SEARCH,
                                KEY_SEARCH_TEXT to searchText
                            )
                        )

                        binding.searchView.hide()
                    }

                }
                TYPE_GET_ALL_SEARCH_QUERIES -> {
                    val resultGetAllSearchQueries = listRequests.find { it.type == TYPE_GET_ALL_SEARCH_QUERIES }?.result?.asSuccess()!!

                    val responseGetAllSearchQueries = resultGetAllSearchQueries.data as ResponseValueModel<*>

                    val _istSearchQueryModel = responseGetAllSearchQueries.value as List<*>
                    val listSearchQueryModel = _istSearchQueryModel.map { it as SearchQueryModel }


                    searchViewModel.fillListSearchQueryModel(
                        listSearchQueryModel = listSearchQueryModel
                    )
                }
                TYPE_DELETE_SEARCH_QUERY -> {
                    searchViewModel.onDeleteSearchQuery()
                }
                TYPE_CLEAR_ALL_SEARCH_HISTORY -> {
                    searchViewModel.onClearAllHistory()
                }

            }

            updateUI(flag = FLAG_SUCCESS_RESULT)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    override fun onErrorResultListener(exception: Exception) {
        val message = getErrorMessage(exception = exception)
        updateUI(FLAG_ERROR_RESULT, messageError = getString(message))
    }

    override fun onLoadingResultListener() {
        updateUI(flag = FLAG_PENDING_RESULT)
    }

    override fun updateUI(flag: String, messageError: String?) = with(binding.layoutPendingResultSearch) {
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

    private fun installSearchHistoryAdapter(listSearchQueryModel: List<SearchQueryModel>) = with(binding){

        val isVisible = listSearchQueryModel.isNotEmpty()

        rvSearchHistory.visibility = if (isVisible) View.VISIBLE else View.GONE

        searchViewModel.installSearchHistoryAdapter {

            searchHistoryAdapter = SearchHistoryAdapter(
                mutableListSearchQueryModel = listSearchQueryModel.toMutableList(),
                onClickSearchQuery = { searchQueryModel ->
                    searchViewModel.onClickEnter(searchText = searchQueryModel.searchText)
                },
                deleteSearchQuery = { searchQueryId ->
                    searchViewModel.deleteSearchQuery(searchQueryId = searchQueryId)
                },
                clearAllHistory = {
                    searchViewModel.clearAllHistory()
                }
            )
            rvSearchHistory.adapter = searchHistoryAdapter
            rvSearchHistory.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun installSearchAdapter(listProductModel: List<ProductModel>) = with(binding){
        searchViewModel.installSearchAdapter(listProductModel = listProductModel){ isVisibleSearch,isVisibleSearchHistory ->

            rvSearch.visibility = if (isVisibleSearch) View.VISIBLE else View.GONE
            rvSearchHistory.visibility = if (isVisibleSearchHistory) View.VISIBLE else View.GONE


            searchAdapter = SearchAdapter(
                listProducts = listProductModel,
                onClick = ::onClickProduct
            )

            rvSearch.adapter = searchAdapter
            rvSearch.layoutManager = LinearLayoutManager(requireContext())
        }

    }

    private fun onClickProduct(productId: Int, title: String) {
        searchViewModel.onClickProduct(productId = productId,searchText = title)
    }
    companion object {
        const val NAVIGATE_TO_PRODUCT_INFO = "NAVIGATE_TO_PRODUCT_INFO"
        const val NAVIGATE_TO_PRODUCTS = "NAVIGATE_TO_PRODUCTS"
    }
}