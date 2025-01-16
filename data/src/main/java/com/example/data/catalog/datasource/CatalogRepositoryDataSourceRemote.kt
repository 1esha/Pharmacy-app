package com.example.data.catalog.datasource

import com.example.data.ResultDataSource

interface CatalogRepositoryDataSourceRemote<T,A,N> {

    suspend fun getAllProducts(): ResultDataSource<T>

    suspend fun getProductsByPath(path: String): ResultDataSource<T>

    suspend fun getPharmacyAddresses(): ResultDataSource<A>

    suspend fun getProductAvailabilityByPath(path: String): ResultDataSource<N>
}