package com.example.data.catalog

import com.example.data.asSuccessResultDataSource
import com.example.data.catalog.datasource.CatalogRepositoryDataSourceRemoteImpl
import com.example.data.toResponseValueListProductModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.ProductModel
import com.example.domain.profile.models.ResponseValueModel

class CatalogRepositoryImpl: CatalogRepository<ResponseValueModel<List<ProductModel>>> {

    private val catalogRepositoryDataSourceRemoteImpl = CatalogRepositoryDataSourceRemoteImpl()

    override suspend fun getAllProducts(): Result<ResponseValueModel<List<ProductModel>>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getAllProducts()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value.toResponseValueListProductModel())

        return result
    }

    override suspend fun getProductsByPath(path: String): Result<ResponseValueModel<List<ProductModel>>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getProductsByPath(path = path)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value.toResponseValueListProductModel())

        return result
    }
}