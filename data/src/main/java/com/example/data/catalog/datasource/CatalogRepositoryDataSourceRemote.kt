package com.example.data.catalog.datasource

import com.example.data.ResultDataSource

interface CatalogRepositoryDataSourceRemote<T> {

    suspend fun getAllProducts(): ResultDataSource<T>

    suspend fun getProductsByPath(path: String): ResultDataSource<T>
}