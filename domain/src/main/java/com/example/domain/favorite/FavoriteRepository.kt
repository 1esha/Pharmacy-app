package com.example.domain.favorite

import com.example.domain.Result
import com.example.domain.favorite.models.FavoriteModel

interface FavoriteRepository<Fa,LFa,Re> {

    suspend fun getAllFavorites(): Result<LFa>

    suspend fun getFavoriteById(productId: Int): Result<Fa>

    suspend fun insertFavorite(favoriteModel: FavoriteModel): Result<Re>

    suspend fun deleteById(productId: Int): Result<Re>

    suspend fun deleteAllFavorite(): Result<Re>

}