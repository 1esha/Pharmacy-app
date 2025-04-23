package com.example.data.advertisements.datasource

import android.util.Log
import com.example.data.ResultDataSource
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.domain.ServerException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AdvertisementRepositoryDataSourceRemoteImpl(
    private val client: HttpClient
): AdvertisementRepositoryDataSourceRemote {

    override fun getHomeAdvertisement(): Flow<ResultDataSource> = flow {
        Log.d("TAG", "getHomeAdvertisement")
        try {
            val response = client.request {
                url(GET_HOME_ADVERTISEMENT)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<String>>>()
            val value = data.value

            if (
                data.responseDataSourceModel.status in 200..299 &&
                value != null
            ){
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else{
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }

    }.flowOn(Dispatchers.IO)

    override fun getRecommendedProducts(): Flow<ResultDataSource> = flow {
        Log.d("TAG", "getRecommendedProducts")
        try {
            val response = client.request {
                url(GET_RECOMMENDED_PRODUCTS)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>>>()
            val value = data.value

            if (
                data.responseDataSourceModel.status in 200..299 &&
                value != null
            ){
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else{
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }

    }.flowOn(Dispatchers.IO)

    companion object {
        private const val GET_HOME_ADVERTISEMENT = "/products/advertisement/home"
        private const val GET_RECOMMENDED_PRODUCTS = "/products/recommended"
    }
}