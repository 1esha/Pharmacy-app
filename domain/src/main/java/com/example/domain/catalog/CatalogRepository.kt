package com.example.domain.catalog

import com.example.domain.Result

interface CatalogRepository<LPr,Av,Ad,Pr> {

    suspend fun getAllProducts(): Result<LPr>

    suspend fun getProductsByPath(path: String): Result<LPr>

    suspend fun getPharmacyAddresses(): Result<Ad>

    suspend fun getProductAvailabilityByPath(path: String): Result<Av>

    suspend fun getProductById(productId: Int): Result<Pr>

}