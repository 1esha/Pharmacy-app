package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductByIdUseCase] является UseCase для получения товара по идентификатору.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 * [productId] - идентификатор товара, который будет получен.
 */
class GetProductByIdUseCase(
    private val catalogRepository: CatalogRepository,
    private val productId: Int
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductByIdFlow(productId = productId)

        return result
    }

}