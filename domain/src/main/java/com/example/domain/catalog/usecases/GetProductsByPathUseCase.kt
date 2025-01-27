package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.ProductModel
import com.example.domain.profile.models.ResponseValueModel

class GetProductsByPathUseCase(
    private val catalogRepository: CatalogRepository<ResponseValueModel<List<ProductModel>?>,*,*>,
    private val path: String
) {

    suspend fun execute(): Result<ResponseValueModel<List<ProductModel>?>> {
        val result = catalogRepository.getProductsByPath(path = path)

        return result
    }

}