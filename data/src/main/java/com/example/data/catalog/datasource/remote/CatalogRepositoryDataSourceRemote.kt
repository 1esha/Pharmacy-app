package com.example.data.catalog.datasource.remote

import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import kotlinx.coroutines.flow.Flow

interface CatalogRepositoryDataSourceRemote {

    fun getAllProductsFlow(): Flow<ResultDataSource>

    fun getProductsByPathFlow(path: String): Flow<ResultDataSource>

    fun getProductByIdFlow(productId: Int): Flow<ResultDataSource>

    fun getProductsByIdsFlow(listIdsProducts: List<Int>): Flow<ResultDataSource>

    fun getProductsBySearchFlow(searchText: String): Flow<ResultDataSource>

    fun getPharmacyAddressesFlow(): Flow<ResultDataSource>

    fun getPharmacyAddressesDetailsFlow(): Flow<ResultDataSource>

    fun getProductAvailabilityByPathFlow(path: String): Flow<ResultDataSource>

    fun getProductAvailabilityByProductIdFlow(productId: Int): Flow<ResultDataSource>

    fun getProductAvailabilityByAddressIdFlow(addressId: Int, listIdsProducts: List<Int>): Flow<ResultDataSource>

    fun getProductAvailabilityByIdsProductsFlow(listIdsProducts: List<Int>): Flow<ResultDataSource>

    fun getProductAvailabilityFlow(): Flow<ResultDataSource>

    fun getOperatingModeFlow(): Flow<ResultDataSource>

    fun updateNumbersProductsInPharmacyFlow(addressId: Int, listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>): Flow<ResultDataSource>
}