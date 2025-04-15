package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductsBySearchUseCase] является UseCase для получения списка товаров с помощью поиска.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом;
 * [searchText] - текст поиска.
 */
class GetProductsBySearchUseCase(
    private val catalogRepository: CatalogRepository,
    private val searchText: String
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductsBySearchFlow(searchText = searchText)

        return result
    }

}