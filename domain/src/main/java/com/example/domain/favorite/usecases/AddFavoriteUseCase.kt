package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.profile.models.ResponseModel

class AddFavoriteUseCase(
    private val favoriteRepository: FavoriteRepository<*,*,ResponseModel>,
    private val favoriteModel: FavoriteModel
) {

    suspend fun execute(): Result<ResponseModel> {
        val result = favoriteRepository.insertFavorite(favoriteModel = favoriteModel)

        return result
    }

}