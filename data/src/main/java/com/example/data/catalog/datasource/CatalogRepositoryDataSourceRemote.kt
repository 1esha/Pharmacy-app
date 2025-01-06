package com.example.data.catalog.datasource

import com.example.data.ResultDataSource

interface CatalogRepositoryDataSourceRemote<T,A> {

    suspend fun getAllProducts(): ResultDataSource<T>

    suspend fun getProductsByPath(path: String): ResultDataSource<T>

    suspend fun getPharmacyAddresses(): ResultDataSource<A>
}