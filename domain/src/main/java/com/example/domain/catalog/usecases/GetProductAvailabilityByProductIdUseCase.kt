package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductAvailabilityByProductIdUseCase] является UseCase для получения списка наличия товара в аптеках по идентификатору товара.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 * [productId] - идентификатор товара наличие которого будет получено.
 */
class GetProductAvailabilityByProductIdUseCase(
    private val catalogRepository: CatalogRepository,
    private val productId: Int
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductAvailabilityByProductIdFlow(productId = productId)

        return result
    }

}