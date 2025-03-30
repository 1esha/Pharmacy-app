package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductAvailabilityUseCase] является UseCase для получения списка наличия товаров в аптеках.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 */
class GetProductAvailabilityUseCase(
    private val catalogRepository: CatalogRepository
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductAvailabilityFlow()

        return result
    }

}