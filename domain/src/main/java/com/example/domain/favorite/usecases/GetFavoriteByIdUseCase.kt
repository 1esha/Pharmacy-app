package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.profile.models.ResponseValueModel

class GetFavoriteByIdUseCase(
    private val favoriteRepository: FavoriteRepository<ResponseValueModel<FavoriteModel>, *, *>,
    private val productId: Int
) {

    suspend fun execute(): Result<ResponseValueModel<FavoriteModel>> {
        val result = favoriteRepository.getFavoriteById(productId = productId)

        return result
    }

}