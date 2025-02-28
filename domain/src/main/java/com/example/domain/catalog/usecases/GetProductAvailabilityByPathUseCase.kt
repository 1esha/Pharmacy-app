package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.profile.models.ResponseValueModel

class GetProductAvailabilityByPathUseCase(
    private val catalogRepository: CatalogRepository<*,ResponseValueModel<List<ProductAvailabilityModel>?>,*,*,*,*>,
    private val path: String
) {

    suspend fun execute(): Result<ResponseValueModel<List<ProductAvailabilityModel>?>> {
        val result = catalogRepository.getProductAvailabilityByPath(path = path)

        return result
    }

}