package com.example.data.orders.datasource

import android.util.Log
import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.domain.ServerException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrdersRepositoryDataSourceRemoteImpl: OrdersRepositoryDataSourceRemote {

    private val client = HttpClient(OkHttp) {
        // URL запроса по умолчанию
        defaultRequest {
            url(BASE_URL)
        }
        install(ContentNegotiation) {
            gson()
        }
    }

    /**
     * Создание заказа.
     *
     * Параметры:
     * [userId] - идентификатор пользователя для которого оформляется заказ;
     * [addressId] - идентификатор аптеки, где будет получен заказ;
     * [listNumberProductsDataSourceModel] - список количества товаров в заказе.
     */
    override fun createOrderFlow(
        userId: Int,
        addressId: Int,
        listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>
    ): Flow<ResultDataSource> = flow{
        Log.d("TAG", "createOrderFlow")
        try {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            val orderDate = LocalDateTime.now().format(formatter)

            val response = client.request {
                url(CREATE_ORDER)
                method = HttpMethod.Post
                setBody(
                    object {
                        val userId = userId
                        val addressId = addressId
                        val orderDate = orderDate
                        val listNumberProducts = listNumberProductsDataSourceModel
                    }
                )
                contentType(type = ContentType.Application.Json)
            }

            val data = response.body<ResponseDataSourceModel>()

            if (
                data.status in 200..299
            ){
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else{
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
        const val CREATE_ORDER = "/orders/make"
    }
}