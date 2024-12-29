package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.profile.models.ResponseValueModel

class GetAllProductsUseCase(
    private val catalogRepository: CatalogRepository<ResponseValueModel<*>>
) {

    suspend fun execute(): Result<ResponseValueModel<*>>{
        val result = catalogRepository.getAllProducts()
        return result
    }

}