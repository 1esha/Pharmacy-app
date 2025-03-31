package com.example.domain.favorite

import com.example.domain.Result
import com.example.domain.favorite.models.FavoriteModel
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [FavoriteRepository] является контрактом для работы с избранными товарами.
 */
interface FavoriteRepository{

    fun getAllFavoritesFlow(): Flow<Result>

    fun getFavoriteByIdFlow(productId: Int): Flow<Result>

    fun insertFavoriteFlow(favoriteModel: FavoriteModel): Flow<Result>

    fun deleteByIdFlow(productId: Int): Flow<Result>

    fun deleteAllFavoriteFlow(): Flow<Result>

}