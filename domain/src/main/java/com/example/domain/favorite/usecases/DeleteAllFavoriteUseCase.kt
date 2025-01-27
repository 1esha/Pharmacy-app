package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.profile.models.ResponseModel

class DeleteAllFavoriteUseCase(private val favoriteRepository: FavoriteRepository<*,*,ResponseModel>) {

    suspend fun execute(): Result<ResponseModel> {
        val result = favoriteRepository.deleteAllFavorite()

        return result
    }

}