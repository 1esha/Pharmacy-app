package com.example.data.favorite.datasource

import com.example.data.ResultDataSource
import com.example.data.favorite.datasource.entity.FavoriteEntity

interface FavoriteRepositoryDataSourceLocal<Fa,LFa,Re> {

    suspend fun getAllFavorites(): ResultDataSource<LFa>

    suspend fun getFavoriteById(productId: Int): ResultDataSource<Fa>

    suspend fun insertFavorite(favoriteEntity: FavoriteEntity): ResultDataSource<Re>

    suspend fun deleteById(productId: Int): ResultDataSource<Re>

    suspend fun deleteAllFavorite(): ResultDataSource<Re>

}