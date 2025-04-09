package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductsByIdsUseCase] является UseCase для получения списка товаров по списку идентификаторов.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом;
 * [listIdsProducts] - список идентификаторов по которому будет получен список товаров.
 */
class GetProductsByIdsUseCase (
    private val catalogRepository: CatalogRepository,
    private val listIdsProducts: List<Int>
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductsByIdsFlow(listIdsProducts = listIdsProducts)

        return result
    }

}