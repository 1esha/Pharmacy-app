package com.example.domain.search.usecases

import com.example.domain.Result
import com.example.domain.search.SearchRepository
import kotlinx.coroutines.flow.Flow

class AddSearchQueryUseCase(
    private val searchRepository: SearchRepository,
    private val searchText: String
) {

    fun execute(): Flow<Result>{
        val result = searchRepository.addSearchQuery(searchText = searchText)

        return result
    }

}