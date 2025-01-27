package com.example.domain.favorite.usecases

import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.profile.models.ResponseModel

class DeleteByIdUseCase(
    private val favoriteRepository: FavoriteRepository<*, *, ResponseModel>,
    private val productId: Int
) {

    suspend fun execute(): Result<ResponseModel> {
        val result = favoriteRepository.deleteById(productId = productId)

        return result
    }

}