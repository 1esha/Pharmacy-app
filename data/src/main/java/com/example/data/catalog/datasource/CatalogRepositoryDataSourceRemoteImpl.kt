package com.example.data.catalog.datasource

import android.util.Log
import com.example.data.ErrorResultDataSource
import com.example.data.ResultDataSource
import com.example.data.SuccessResultDataSource
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

class CatalogRepositoryDataSourceRemoteImpl: CatalogRepositoryDataSourceRemote<ResponseValueDataSourceModel<*>> {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun getAllProducts(): ResultDataSource<ResponseValueDataSourceModel<*>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_ALL_PRODUCTS_URL)
                    method = HttpMethod.Get
                }

                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<*>>()
                Log.i("TAG","getAllProducts responseValueDataSourceModel = $responseValueDataSourceModel")
                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG","getAllProducts successResultDataSource = $successResultDataSource")
                return@withContext successResultDataSource
            }
            catch (e: Exception){
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<*>>(
                    exception = e
                )
                Log.i("TAG","getAllProducts errorResultDataSource = ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }

        }

    override suspend fun getProductsByPath(path: String): ResultDataSource<ResponseValueDataSourceModel<*>> {
        TODO("Not yet implemented")
    }


    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.113:$PORT"
        const val GET_ALL_PRODUCTS_URL = "$BASE_URL/products"
    }
}