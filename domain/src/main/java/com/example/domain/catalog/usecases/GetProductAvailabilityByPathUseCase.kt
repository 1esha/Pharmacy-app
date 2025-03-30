package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductAvailabilityByPathUseCase] является UseCase для получения списка наличия товаров по переданному пути.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 * [path] - путь по которому будет получен список данных о наличии товаров в аптеках.
 */
class GetProductAvailabilityByPathUseCase(
    private val catalogRepository: CatalogRepository,
    private val path: String
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductAvailabilityByPathFlow(path = path)

        return result
    }

}