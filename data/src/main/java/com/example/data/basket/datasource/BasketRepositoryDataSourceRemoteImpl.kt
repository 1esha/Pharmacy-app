package com.example.data.basket.datasource

import android.util.Log
import com.example.data.ErrorResultDataSource
import com.example.data.ResultDataSource
import com.example.data.SuccessResultDataSource
import com.example.data.basket.datasource.models.BasketDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Класс [BasketRepositoryDataSourceRemoteImpl] является реализацией интерфейса [BasketRepositoryDataSourceRemote].
 */
class BasketRepositoryDataSourceRemoteImpl: BasketRepositoryDataSourceRemote<
        ResponseDataSourceModel,
        ResponseValueDataSourceModel<List<BasketDataSourceModel>>,
        ResponseValueDataSourceModel<List<Int>>
        > {

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
    override suspend fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int): ResultDataSource<ResponseDataSourceModel> = withContext(Dispatchers.IO) {

        try {
            val response = client.request {

                url(ADD_PRODUCT_IN_BASKET)

                parameter("user_id",userId)
                parameter("product_id",productId)
                parameter("number_products",numberProducts)

                method = HttpMethod.Get
            }

            val responseDataSourceModel = response.body<ResponseDataSourceModel>()

            val successResultDataSource = SuccessResultDataSource(
                value = responseDataSourceModel
            )
            Log.i("TAG", "addProductInBasket successResultDataSource ${successResultDataSource.value}")
            return@withContext successResultDataSource
        }
        catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                exception = e
            )
            Log.i("TAG", "addProductInBasket errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }

    }

    /**
     * Удаление товара из коризины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будет удален товар;
     * [productId] - идентификатор товара для удаления.
     */
    override suspend fun deleteProductFromBasket(userId: Int, productId: Int): ResultDataSource<ResponseDataSourceModel> = withContext(Dispatchers.IO) {

        try {
            val response = client.request {

                url(DELETE_PRODUCT_FROM_BASKET)

                parameter("user_id",userId)
                parameter("product_id",productId)

                method = HttpMethod.Get
            }

            val responseDataSourceModel = response.body<ResponseDataSourceModel>()

            val successResultDataSource = SuccessResultDataSource(
                value = responseDataSourceModel
            )
            Log.i("TAG", "deleteProductFromBasket successResultDataSource ${successResultDataSource.value}")
            return@withContext successResultDataSource
        }
        catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                exception = e
            )
            Log.i("TAG", "deleteProductFromBasket errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }

    }

    /**
     * Получение списка идентификаторов товаров, находящихся в коризине пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override suspend fun getIdsProductsFromBasket(userId: Int): ResultDataSource<ResponseValueDataSourceModel<List<Int>>> = withContext(Dispatchers.IO) {

        try {
            val response = client.request {

                url(GET_IDS_PRODUCTS_FROM_BASKET)

                parameter("user_id",userId)

                method = HttpMethod.Get
            }

            val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<Map<String,Int>>>>()

            // Преобразование List<Map<String,Int> в List<Int>,
            // где List<Int> это список идентификаторов товаров из корзины пользователя
            val _values = responseValueDataSourceModel.value

            val values = mutableListOf<Int>()

            if (_values != null) {

                _values.forEach { map ->
                    val value = map.values.first()

                    values.add(value)
                }

            }

            val successResultDataSource = SuccessResultDataSource(
                value = ResponseValueDataSourceModel(
                    value = values.toList(),
                    responseDataSourceModel = responseValueDataSourceModel.responseDataSourceModel
                )
            )
            Log.i("TAG", "getIdsProductsFromBasket successResultDataSource ${successResultDataSource.value.value}")
            return@withContext successResultDataSource
        }
        catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<Int>>>(
                exception = e
            )
            Log.i("TAG", "getIdsProductsFromBasket errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }

    }

    /**
     * Получение списка товаров из корины пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override suspend fun getProductsFromBasket(userId: Int): ResultDataSource<ResponseValueDataSourceModel<List<BasketDataSourceModel>>> = withContext(Dispatchers.IO) {

        try {
            val response = client.request {

                url(GET_PRODUCTS_FROM_BASKET)

                parameter("user_id",userId)

                method = HttpMethod.Get
            }

            val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<List<BasketDataSourceModel>>>()

            val successResultDataSource = SuccessResultDataSource(
                value = responseValueDataSourceModel
            )
            Log.i("TAG", "getProductsFromBasket successResultDataSource ${successResultDataSource.value.value}")
            return@withContext successResultDataSource
        }
        catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<List<BasketDataSourceModel>>>(
                exception = e
            )
            Log.i("TAG", "getProductsFromBasket errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }

    }

    /**
     * Обновление количество товара в корзине.
     *
     * Параметры:
     * [userId] - идентификатор пользователя;
     * [productId] - идентификатор товара;
     * [numberProducts] - новое количество товара в корзине.
     */
    override suspend fun updateNumberProductsInBasket(userId: Int, productId: Int, numberProducts: Int): ResultDataSource<ResponseDataSourceModel> = withContext(Dispatchers.IO) {

        try {
            val response = client.request {

                url(UPDATE_NUMBER_PRODUCTS_IN_BASKET)

                parameter("user_id",userId)
                parameter("product_id",productId)
                parameter("number_products",numberProducts)

                method = HttpMethod.Get
            }

            val responseDataSourceModel = response.body<ResponseDataSourceModel>()

            val successResultDataSource = SuccessResultDataSource(
                value = responseDataSourceModel
            )
            Log.i("TAG", "updateNumberProductsInBasket successResultDataSource ${successResultDataSource.value}")
            return@withContext successResultDataSource
        }
        catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                exception = e
            )
            Log.i("TAG", "updateNumberProductsInBasket errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }

    }

    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
        private const val ADD_PRODUCT_IN_BASKET = "/basket/add"
        private const val DELETE_PRODUCT_FROM_BASKET = "/basket/delete"
        private const val GET_IDS_PRODUCTS_FROM_BASKET = "/basket/user/products_id"
        private const val GET_PRODUCTS_FROM_BASKET = "/basket/user"
        private const val UPDATE_NUMBER_PRODUCTS_IN_BASKET = "/basket/update"
    }
}