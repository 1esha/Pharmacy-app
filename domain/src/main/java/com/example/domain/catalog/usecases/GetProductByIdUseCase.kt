package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.ProductModel
import com.example.domain.profile.models.ResponseValueModel

class GetProductByIdUseCase(
    private val catalogRepository: CatalogRepository<*, *, *, ResponseValueModel<ProductModel?>>,
    private val productId: Int
) {

    suspend fun execute(): Result<ResponseValueModel<ProductModel?>> {
        val result = catalogRepository.getProductById(productId = productId)

        return result
    }

}