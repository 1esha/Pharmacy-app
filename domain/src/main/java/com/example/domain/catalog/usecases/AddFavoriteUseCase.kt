package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.FavoriteModel
import com.example.domain.profile.models.ResponseModel

class AddFavoriteUseCase(
    private val catalogRepository: CatalogRepository<*,*,*,*,*,ResponseModel>,
    private val favoriteModel: FavoriteModel
) {

    suspend fun execute(): Result<ResponseModel> {
        val result = catalogRepository.addFavorite(favoriteModel = favoriteModel)

        return result
    }

}