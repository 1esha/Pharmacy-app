package com.example.data.catalog

import com.example.data.asSuccessResultDataSource
import com.example.data.catalog.datasource.remote.CatalogRepositoryDataSourceRemoteImpl
import com.example.data.toResponseValueListPharmacyAddressesModel
import com.example.data.toResponseValueListProductAvailabilityModel
import com.example.data.toResponseValueListProductModel
import com.example.data.toResponseValueProductModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel

class CatalogRepositoryImpl() : CatalogRepository<
        ResponseValueModel<List<ProductModel>?>,
        ResponseValueModel<List<ProductAvailabilityModel>?>,
        ResponseValueModel<List<PharmacyAddressesModel>?>,
        ResponseValueModel<ProductModel?>
        >{


    private val catalogRepositoryDataSourceRemoteImpl = CatalogRepositoryDataSourceRemoteImpl()

    override suspend fun getAllProducts(): Result<ResponseValueModel<List<ProductModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getAllProducts()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductModel())

        return result
    }

    override suspend fun getProductsByPath(path: String): Result<ResponseValueModel<List<ProductModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getProductsByPath(path = path)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductModel())

        return result
    }

    override suspend fun getPharmacyAddresses(): Result<ResponseValueModel<List<PharmacyAddressesModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getPharmacyAddresses()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListPharmacyAddressesModel())

        return result
    }

    override suspend fun getProductAvailabilityByPath(path: String): Result<ResponseValueModel<List<ProductAvailabilityModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByPath(path = path)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductAvailabilityModel())

        return result
    }

    override suspend fun getProductById(productId: Int): Result<ResponseValueModel<ProductModel?>> {
        val responseValueModel = catalogRepositoryDataSourceRemoteImpl.getProductById(productId = productId)
        val value = responseValueModel.asSuccessResultDataSource()?.value
        val result = responseValueModel.toResult(
            value = value?.toResponseValueProductModel()
        )

        return result
    }

    override suspend fun getProductAvailabilityByProductId(productId: Int): Result<ResponseValueModel<List<ProductAvailabilityModel>?>> {
        val responseValueModel = catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByProductId(productId = productId)
        val value = responseValueModel.asSuccessResultDataSource()?.value
        val result = responseValueModel.toResult(
            value = value?.toResponseValueListProductAvailabilityModel()
        )

        return result
    }

}