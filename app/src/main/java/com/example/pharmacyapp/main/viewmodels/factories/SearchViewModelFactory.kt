package com.example.pharmacyapp.main.viewmodels.factories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.catalog.CatalogRepositoryImpl
import com.example.data.search.SearchRepositoryImpl
import com.example.pharmacyapp.main.viewmodels.SearchViewModel

class SearchViewModelFactory(context: Context): ViewModelProvider.Factory {

    private val catalogRepositoryImpl = CatalogRepositoryImpl()

    private val searchRepositoryImpl = SearchRepositoryImpl(context = context)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(
            catalogRepositoryImpl = catalogRepositoryImpl,
            searchRepositoryImpl = searchRepositoryImpl
        ) as T
    }

}