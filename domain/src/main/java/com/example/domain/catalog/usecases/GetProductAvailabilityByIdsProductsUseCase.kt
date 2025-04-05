package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductAvailabilityByIdsProductsUseCase] является UseCase для получения списка наличия товаров по списку идентификаторов.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 * [listIdsProducts] - список идентификаторов по которому будет получен список данных о наличии товаров в аптеках.
 */
class GetProductAvailabilityByIdsProductsUseCase(
    private val catalogRepository: CatalogRepository,
    private val listIdsProducts: List<Int>
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductAvailabilityByIdsProductsFlow(listIdsProducts = listIdsProducts)

        return result
    }

}