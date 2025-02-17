package com.example.data.catalog.datasource.remote

import android.util.Log
import com.example.data.ErrorResultDataSource
import com.example.data.ResultDataSource
import com.example.data.SuccessResultDataSource
import com.example.data.catalog.datasource.models.PharmacyAddressesDataSourceModel
import com.example.data.catalog.datasource.models.ProductAvailabilityDataSourceModel
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CatalogRepositoryDataSourceRemoteImpl: CatalogRepositoryDataSourceRemote<
        ResponseValueDataSourceModel<List<ProductDataSourceModel>?>,
        ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>,
        ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>?>,
        ResponseValueDataSourceModel<ProductDataSourceModel?>
        > {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun getAllProducts(): ResultDataSource<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_ALL_PRODUCTS_URL)
                    method = HttpMethod.Get
                }

                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>>()
                Log.i("TAG","getAllProducts responseValueDataSourceModel = $responseValueDataSourceModel")
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getAllProducts successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception){
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>>(
                    exception = e
                )
                Log.i("TAG","getAllProducts errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }

        }

    override suspend fun getProductsByPath(path: String): ResultDataSource<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_PRODUCTS_BY_PATH +path)
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>>()
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getProductsByPath successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<ProductDataSourceModel>?>>(
                    exception = e
                )
                Log.i("TAG","getProductsByPath errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getPharmacyAddresses(): ResultDataSource<ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_PHARMACY_ADDRESSES)
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>?>>()
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getPharmacyAddresses successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>?>>(
                    exception = e
                )
                Log.i("TAG","getPharmacyAddresses errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getProductAvailabilityByPath(path: String): ResultDataSource<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_PRODUCT_AVAILABILITY_BY_PATH +path)
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>>()
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getProductAvailabilityByPath successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>>(
                    exception = e
                )
                Log.i("TAG","getProductAvailabilityByPath errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getProductById(productId: Int): ResultDataSource<ResponseValueDataSourceModel<ProductDataSourceModel?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_PRODUCT_BY_ID+productId)
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<ProductDataSourceModel?>>()
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getProductById successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<ProductDataSourceModel?>>(
                    exception = e
                )
                Log.i("TAG","getProductById errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getProductAvailabilityByProductId(productId: Int): ResultDataSource<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID+productId)
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>>()
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getProductAvailabilityByProductId successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>>(
                    exception = e
                )
                Log.i("TAG","getProductAvailabilityByProductId errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
        const val GET_ALL_PRODUCTS_URL = "$BASE_URL/products"
        const val GET_PRODUCTS_BY_PATH = "$BASE_URL/products/path?path="
        const val GET_PHARMACY_ADDRESSES = "$BASE_URL/pharmacy/addresses"
        const val GET_PRODUCT_AVAILABILITY_BY_PATH = "$BASE_URL/availability?path="
        const val GET_PRODUCT_BY_ID = "$BASE_URL/product/id?id="
        const val GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID = "$BASE_URL/availability/product_id?id="
    }
}