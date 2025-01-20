package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.FavoriteModel
import com.example.domain.profile.models.ResponseValueModel

class GetAllFavoritesUseCase(
    private val catalogRepository: CatalogRepository<*,*,*,*,ResponseValueModel<List<FavoriteModel>>,*>
) {

    suspend fun execute(): Result<ResponseValueModel<List<FavoriteModel>>> {
        val result = catalogRepository.getAllFavorites()

        return result
    }

}