package com.example.domain.catalog

import com.example.domain.Result
import com.example.domain.models.NumberProductsModel
import kotlinx.coroutines.flow.Flow

interface CatalogRepository{

    fun getAllProductsFlow(): Flow<Result>

    fun getProductsByPathFlow(path: String): Flow<Result>

    fun getProductByIdFlow(productId: Int): Flow<Result>

    fun getProductsByIdsFlow(listIdsProducts: List<Int>): Flow<Result>

    fun getPharmacyAddressesFlow(): Flow<Result>

    fun getPharmacyAddressesDetailsFlow(): Flow<Result>

    fun getProductAvailabilityByPathFlow(path: String): Flow<Result>

    fun getProductAvailabilityByProductIdFlow(productId: Int): Flow<Result>

    fun getProductAvailabilityByAddressIdFlow(addressId: Int, listIdsProducts: List<Int>): Flow<Result>

    fun getProductAvailabilityByIdsProductsFlow(listIdsProducts: List<Int>): Flow<Result>

    fun getProductAvailabilityFlow(): Flow<Result>

    fun getOperatingModeFlow(): Flow<Result>

    fun updateNumbersProductsInPharmacyFlow(addressId: Int, listNumberProductsModel: List<NumberProductsModel>): Flow<Result>

}