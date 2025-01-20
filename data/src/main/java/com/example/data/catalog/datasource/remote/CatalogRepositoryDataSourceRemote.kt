package com.example.data.catalog.datasource.remote

import com.example.data.ResultDataSource

interface CatalogRepositoryDataSourceRemote<Pr,Av,Ad> {

    suspend fun getAllProducts(): ResultDataSource<Pr>

    suspend fun getProductsByPath(path: String): ResultDataSource<Pr>

    suspend fun getPharmacyAddresses(): ResultDataSource<Ad>

    suspend fun getProductAvailabilityByPath(path: String): ResultDataSource<Av>
}