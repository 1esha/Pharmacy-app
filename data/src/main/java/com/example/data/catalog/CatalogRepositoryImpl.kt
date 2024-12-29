package com.example.data.catalog

import com.example.data.asSuccessResultDataSource
import com.example.data.catalog.datasource.CatalogRepositoryDataSourceRemoteImpl
import com.example.data.toResponseValueModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.profile.models.ResponseValueModel

class CatalogRepositoryImpl: CatalogRepository<ResponseValueModel<*>> {

    private val catalogRepositoryDataSourceRemoteImpl = CatalogRepositoryDataSourceRemoteImpl()

    override suspend fun getAllProducts(): Result<ResponseValueModel<*>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getAllProducts()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value.toResponseValueModel())

        return result
    }

    override suspend fun getProductsByPath(path: String): Result<ResponseValueModel<*>> {
        TODO("Not yet implemented")
    }
}