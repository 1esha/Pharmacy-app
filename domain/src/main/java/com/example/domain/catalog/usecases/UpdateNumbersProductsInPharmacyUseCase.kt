package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.models.NumberProductsModel
import kotlinx.coroutines.flow.Flow


/**
 * Класс [UpdateNumbersProductsInPharmacyUseCase] является UseCase для обновления количества товаров в аптеке.
 *
 * Параметры:
 * [catalogRepository] -  репозиторий с функционалом;
 * [addressId] - идентификатор аптеки;
 * [listNumberProductsModel] - список с новым количеством товаров.
 */
class UpdateNumbersProductsInPharmacyUseCase(
    private val catalogRepository: CatalogRepository,
    private val addressId: Int,
    private val listNumberProductsModel: List<NumberProductsModel>
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.updateNumbersProductsInPharmacyFlow(
            addressId = addressId,
            listNumberProductsModel = listNumberProductsModel
        )

        return result
    }

}