package com.example.data.catalog.datasource.remote

import com.example.data.ResultDataSource

interface CatalogRepositoryDataSourceRemote<LPr,Av,Ad,Pr> {

    suspend fun getAllProducts(): ResultDataSource<LPr>

    suspend fun getProductsByPath(path: String): ResultDataSource<LPr>

    suspend fun getPharmacyAddresses(): ResultDataSource<Ad>

    suspend fun getProductAvailabilityByPath(path: String): ResultDataSource<Av>

    suspend fun getProductById(productId: Int): ResultDataSource<Pr>
}