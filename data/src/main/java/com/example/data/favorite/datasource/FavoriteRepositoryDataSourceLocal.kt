package com.example.data.favorite.datasource

import com.example.data.ResultDataSource
import com.example.data.favorite.datasource.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс [FavoriteRepositoryDataSourceLocal] является контрактом для работы с избранными товарами в data слое.
 */
interface FavoriteRepositoryDataSourceLocal{

    fun getAllFavoritesFlow(): Flow<ResultDataSource>

    fun getFavoriteByIdFlow(productId: Int): Flow<ResultDataSource>

    fun insertFavoriteFlow(favoriteEntity: FavoriteEntity): Flow<ResultDataSource>

    fun deleteByIdFlow(productId: Int): Flow<ResultDataSource>

    fun deleteAllFavoriteFlow(): Flow<ResultDataSource>

}