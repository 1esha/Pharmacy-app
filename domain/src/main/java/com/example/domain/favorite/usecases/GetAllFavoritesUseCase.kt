package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.profile.models.ResponseValueModel

class GetAllFavoritesUseCase(
    private val favoriteRepository: FavoriteRepository<*, ResponseValueModel<List<FavoriteModel>>, *>,
) {

    suspend fun execute(): Result<ResponseValueModel<List<FavoriteModel>>> {
        val result = favoriteRepository.getAllFavorites()

        return result
    }

}