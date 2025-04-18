package com.example.domain.search.usecases

import com.example.domain.Result
import com.example.domain.search.SearchRepository
import kotlinx.coroutines.flow.Flow

class GetAllSearchQueriesUseCase(
    private val searchRepository: SearchRepository
) {

    fun execute(): Flow<Result> {
        val result = searchRepository.getAllSearchQueries()

        return result
    }

}