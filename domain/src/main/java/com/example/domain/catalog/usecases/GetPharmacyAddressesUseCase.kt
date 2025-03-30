package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetPharmacyAddressesUseCase] является UseCase для получения списка данных о аптеках.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 */
class GetPharmacyAddressesUseCase(
    private val catalogRepository: CatalogRepository
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getPharmacyAddressesFlow()

        return result
    }

}