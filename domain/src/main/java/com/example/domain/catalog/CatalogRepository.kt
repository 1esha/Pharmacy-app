package com.example.domain.catalog

import com.example.domain.Result
import com.example.domain.catalog.models.FavoriteModel

interface CatalogRepository<Pr,Av,Ad,Fa,LFa,Re> {

    suspend fun getAllProducts(): Result<Pr>

    suspend fun getProductsByPath(path: String): Result<Pr>

    suspend fun getPharmacyAddresses(): Result<Ad>

    suspend fun getProductAvailabilityByPath(path: String): Result<Av>

    suspend fun getAllFavorites(): Result<LFa>

    suspend fun getFavoriteById(productId: Int): Result<Fa>

    suspend fun addFavorite(favoriteModel: FavoriteModel): Result<Re>

    suspend fun deleteById(productId: Int): Result<Re>
}