package com.example.domain.search

import com.example.domain.Result
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun addSearchQuery(searchText: String): Flow<Result>

    fun deleteSearchQuery(searchQueryId: Int): Flow<Result>

    fun getAllSearchQueries(): Flow<Result>

    fun deleteAllSearchQueries(): Flow<Result>
}