package com.example.data.catalog.datasource.local

import com.example.data.ResultDataSource
import com.example.data.catalog.datasource.local.entity.FavoriteEntity

interface CatalogRepositoryDataSourceLocal<Fa,LFa,Re> {

    suspend fun getAllFavorites(): ResultDataSource<LFa>

    suspend fun getFavoriteById(productId: Int): ResultDataSource<Fa>

    suspend fun insertFavorite(favoriteEntity: FavoriteEntity): ResultDataSource<Re>

    suspend fun deleteById(productId: Int): ResultDataSource<Re>

}