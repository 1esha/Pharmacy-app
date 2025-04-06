package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetProductAvailabilityByAddressIdUseCase] является UseCase для получения списка наличия товаров в текущей аптеке.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом;
 * [addressId] - идентификатор аптеки из которой будет получен список наличия товаров;
 * [listIdsProducts] - список идентификаторов товаров, которые будут выбраны в текущей аптеке.
 */
class GetProductAvailabilityByAddressIdUseCase(
    private val catalogRepository: CatalogRepository,
    private val addressId: Int,
    private val listIdsProducts: List<Int>
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getProductAvailabilityByAddressIdFlow(
            addressId = addressId,
            listIdsProducts = listIdsProducts
        )

        return result
    }

}
