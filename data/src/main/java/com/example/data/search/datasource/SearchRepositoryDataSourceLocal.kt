package com.example.data.search.datasource

import com.example.data.ResultDataSource
import kotlinx.coroutines.flow.Flow

interface SearchRepositoryDataSourceLocal {

    fun insertSearchQuery(searchText: String): Flow<ResultDataSource>

    fun deleteSearchQuery(searchQueryId: Int): Flow<ResultDataSource>

    fun getAllSearchQueries(): Flow<ResultDataSource>

    fun deleteAllSearchQueries(): Flow<ResultDataSource>

}