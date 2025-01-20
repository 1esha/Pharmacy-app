package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.profile.models.ResponseModel

class DeleteByIdUseCase(
    private val catalogRepository: CatalogRepository<*,*,*,*,*,ResponseModel>,
    private val productId: Int
) {

    suspend fun execute(): Result<ResponseModel> {
        val result = catalogRepository.deleteById(productId = productId)

        return result
    }

}