package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetPharmacyAddressesDetailsUseCase] является UseCase для получения списка подробной информации о аптеках.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 */
class GetPharmacyAddressesDetailsUseCase(
    private val catalogRepository: CatalogRepository
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getPharmacyAddressesDetailsFlow()

        return result
    }

}