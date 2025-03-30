package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetAllProductsUseCase] является UseCase для получения всех товаров.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 */
class GetAllProductsUseCase(
    private val catalogRepository: CatalogRepository
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getAllProductsFlow()
        return result
    }

}