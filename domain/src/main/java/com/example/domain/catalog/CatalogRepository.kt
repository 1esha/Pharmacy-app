package com.example.domain.catalog

import com.example.domain.Result

interface CatalogRepository<Pr,Av,Ad> {

    suspend fun getAllProducts(): Result<Pr>

    suspend fun getProductsByPath(path: String): Result<Pr>

    suspend fun getPharmacyAddresses(): Result<Ad>

    suspend fun getProductAvailabilityByPath(path: String): Result<Av>

}