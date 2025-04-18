package com.example.domain.search.usecases

import com.example.domain.Result
import com.example.domain.search.SearchRepository
import kotlinx.coroutines.flow.Flow

class DeleteSearchQueryUseCase(
    private val searchRepository: SearchRepository,
    private val searchQueryId: Int
) {

    fun execute(): Flow<Result> {
        val result = searchRepository.deleteSearchQuery(searchQueryId = searchQueryId)

        return result
    }

}