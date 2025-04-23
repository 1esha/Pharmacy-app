package com.example.data.advertisements

import android.util.Log
import com.example.data.HttpClient
import com.example.data.advertisements.datasource.AdvertisementRepositoryDataSourceRemoteImpl
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toListProductModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.advertisements.AdvertisementRepository
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AdvertisementRepositoryImpl: AdvertisementRepository {

    private val client = HttpClient().client

    private val advertisementRepositoryDataSourceRemoteImpl = AdvertisementRepositoryDataSourceRemoteImpl(client = client)

    override fun getHomeAdvertisement(): Flow<Result> = flow {
        try {
            advertisementRepositoryDataSourceRemoteImpl.getHomeAdvertisement()
                .collect{ resultDataSource ->
                    val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                    if (response != null) {
                        val _listAdvertisements = response.value as List<*>
                        val listAdvertisements = _listAdvertisements.map {
                            return@map it as String
                        }

                        val data = ResponseValueModel(
                            value = listAdvertisements,
                            responseModel = response.responseDataSourceModel.toResponseModel()
                        )
                        emit(Result.Success(data = data))
                    }
                    else {
                        val resultError = resultDataSource.asError()
                        if (resultError != null) {
                            emit(Result.Error(exception = resultError.exception))
                        }
                        else throw IllegalArgumentException("Несуществующий тип результата")
                    }
                }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }

    }.flowOn(Dispatchers.IO)

    override fun getRecommendedProducts(): Flow<Result> = flow {
        try {
            advertisementRepositoryDataSourceRemoteImpl.getRecommendedProducts()
                .collect{ resultDataSource ->
                    val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                    if (response != null) {
                        val _listProductDataSourceModel = response.value as List<*>
                        val listProductDataSourceModel = _listProductDataSourceModel.map {
                            return@map it as ProductDataSourceModel
                        }

                        val listProductModel = listProductDataSourceModel.toListProductModel()

                        val data = ResponseValueModel(
                            value = listProductModel,
                            responseModel = response.responseDataSourceModel.toResponseModel()
                        )
                        emit(Result.Success(data = data))
                    }
                    else {
                        val resultError = resultDataSource.asError()
                        if (resultError != null) {
                            emit(Result.Error(exception = resultError.exception))
                        }
                        else throw IllegalArgumentException("Несуществующий тип результата")
                    }
                }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }

    }.flowOn(Dispatchers.IO)
}