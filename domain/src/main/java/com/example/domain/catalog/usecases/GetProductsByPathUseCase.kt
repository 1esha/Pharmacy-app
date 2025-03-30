package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductsByPathUseCase] является UseCase для получения списка товаров по переданному пути.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 * [path] - путь по которому будет получен список товаров.
 */
class GetProductsByPathUseCase(
    private val catalogRepository: CatalogRepository,
    private val path: String
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductsByPathFlow(path = path)

        return result
    }

}