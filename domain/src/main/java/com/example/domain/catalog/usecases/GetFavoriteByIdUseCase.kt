package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.FavoriteModel
import com.example.domain.profile.models.ResponseValueModel

class GetFavoriteByIdUseCase(
    private val catalogRepository: CatalogRepository<*, *, *, ResponseValueModel<FavoriteModel>, *, *>,
    private val productId: Int
) {

    suspend fun execute(): Result<ResponseValueModel<FavoriteModel>> {
        val result = catalogRepository.getFavoriteById(productId = productId)

        return result
    }

}