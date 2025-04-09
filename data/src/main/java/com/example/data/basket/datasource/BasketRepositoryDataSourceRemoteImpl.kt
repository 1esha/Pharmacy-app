package com.example.data.basket.datasource

import android.util.Log
import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.BasketDataSourceModel
import com.example.data.basket.datasource.models.DeleteProductsFromBasketDataSourceModel
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.domain.ServerException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.parameter
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

/**
 * Класс [BasketRepositoryDataSourceRemoteImpl] является реализацией интерфейса [BasketRepositoryDataSourceRemote].
 */
class BasketRepositoryDataSourceRemoteImpl: BasketRepositoryDataSourceRemote {

    /**
     * Создание клиента.
     */
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
     * Добавление товара в корзину.
     *
     * Параметры:
     * [userId] - идентификатор пользователя в чью корзину будет добавлен товар;
     * [productId] - идентификатор товара для добавления;
     * [numberProducts] - количество товра, которое будет добавлено в коризну.
     */
    override fun addProductInBasketFlow(
        userId: Int,
        productId: Int,
        numberProducts: Int
    ): Flow<ResultDataSource> = flow {
        Log.d("TAG", "addProductInBasketFlow")
        try {
            val response = client.request {
                url(ADD_PRODUCT_IN_BASKET)

                parameter("user_id",userId)
                parameter("product_id",productId)
                parameter("number_products",numberProducts)

                method = HttpMethod.Get
            }

            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299){
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else{
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.message)))
            }

        }
        catch (e: Exception){
            Log.d("TAG","ER2")
            emit(ResultDataSource.Error(exception = e))
        }

    }.flowOn(Dispatchers.IO)

    /**
     * Удаление товара из коризины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будет удален товар;
     * [productId] - идентификатор товара для удаления.
     */
    override fun deleteProductFromBasketFlow(
        userId: Int,
        productId: Int
    ): Flow<ResultDataSource> = flow{
        Log.d("TAG", "deleteProductFromBasketFlow")
        try {
            val response = client.request {
                url(DELETE_PRODUCT_FROM_BASKET)
                parameter("user_id",userId)
                parameter("product_id",productId)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299){
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

    /**
     * Получение списка идентификаторов товаров, находящихся в коризине пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getIdsProductsFromBasketFlow(userId: Int): Flow<ResultDataSource> = flow {
        Log.d("TAG", "getIdsProductsFromBasketFlow")
        try {
            val response = client.request {
                url(GET_IDS_PRODUCTS_FROM_BASKET)
                parameter("user_id",userId)
                method = HttpMethod.Get
            }
            val _data = response.body<ResponseValueDataSourceModel<List<Map<String,Int>>>>()

            if (
                _data.responseDataSourceModel.status in 200..299 &&
                _data.value != null
            ){
                val listMap = _data.value
                val mutableListIdsProductsFromBasket = mutableListOf<Int>()

                listMap.forEach { map ->
                    val value = map.values.first()

                    mutableListIdsProductsFromBasket.add(value)
                }

                val data = ResponseValueDataSourceModel(
                    value = mutableListIdsProductsFromBasket,
                    responseDataSourceModel = _data.responseDataSourceModel
                )

                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else{
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = _data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка товаров из корины пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getProductsFromBasketFlow(userId: Int): Flow<ResultDataSource> = flow {
        Log.d("TAG", "getProductsFromBasketFlow")
        try {
            val response = client.request {
                url(GET_PRODUCTS_FROM_BASKET)
                parameter("user_id",userId)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<BasketDataSourceModel>>>()
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

    /**
     * Удаление нескольких товаров из коризины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будут удалены товары;
     * [listIdsProducts] - список идентификаторов товаров для удаления.
     */
    override fun deleteProductsFromBasketFlow(
        userId: Int,
        listIdsProducts: List<Int>
    ): Flow<ResultDataSource> = flow{
        Log.d("TAG", "deleteProductsFromBasket")
        try {
            val response = client.request {
                url(DELETE_PRODUCTS_FROM_BASKET)
                method = HttpMethod.Post
                setBody(DeleteProductsFromBasketDataSourceModel(
                    userId = userId,
                    listIdsProducts = listIdsProducts
                ))
                contentType(type = ContentType.Application.Json)
            }

            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299){
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

    /**
     * Обновление количество товара в корзине.
     *
     * Параметры:
     * [userId] - идентификатор пользователя;
     * [productId] - идентификатор товара;
     * [numberProducts] - новое количество товара в корзине.
     */
    override fun updateNumberProductsInBasketFlow(
        userId: Int,
        productId: Int,
        numberProducts: Int
    ): Flow<ResultDataSource> = flow {
        Log.d("TAG", "updateNumberProductsInBasketFlow")
        try {
            val response = client.request {
                url(UPDATE_NUMBER_PRODUCTS_IN_BASKET)
                parameter("user_id",userId)
                parameter("product_id",productId)
                parameter("number_products",numberProducts)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299){
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

    /**
     * Обновление количества товаров в корзине.
     *
     * Параметры:
     * [userId] - идентификатор пользователя;
     * [listNumberProductsDataSourceModel] - список с новым количеством товаров.
     */
    override fun updateNumbersProductsInBasketFlow(
        userId: Int,
        listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>
    ): Flow<ResultDataSource> = flow {
        Log.d("TAG", "updateNumbersProductsInBasketFlow")
        try {
            val response = client.request {
                url(UPDATE_NUMBERS_PRODUCTS_IN_BASKET)
                method = HttpMethod.Post
                setBody(
                    object {
                        val userId = userId
                        val listNumberProducts = listNumberProductsDataSourceModel
                    }
                )
                contentType(ContentType.Application.Json)
            }

            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299){
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
        private const val ADD_PRODUCT_IN_BASKET = "/basket/add"
        private const val DELETE_PRODUCT_FROM_BASKET = "/basket/delete"
        private const val DELETE_PRODUCTS_FROM_BASKET = "/basket/delete/products"
        private const val GET_IDS_PRODUCTS_FROM_BASKET = "/basket/user/products_id"
        private const val GET_PRODUCTS_FROM_BASKET = "/basket/user"
        private const val UPDATE_NUMBER_PRODUCTS_IN_BASKET = "/basket/update"
        private const val UPDATE_NUMBERS_PRODUCTS_IN_BASKET = "/basket/update/products"
    }
}