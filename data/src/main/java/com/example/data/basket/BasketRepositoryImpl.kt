package com.example.data.basket

import android.util.Log
import com.example.data.HttpClient
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.basket.datasource.BasketRepositoryDataSourceRemoteImpl
import com.example.data.basket.datasource.models.BasketDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toListNumberProductsDataSourceModel
import com.example.data.toProductModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import com.example.domain.basket.models.BasketModel
import com.example.domain.models.NumberProductsModel
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Класс [BasketRepositoryImpl] является реализвцией интерфейса [BasketRepository].
 */
class BasketRepositoryImpl: BasketRepository {

    private val client = HttpClient().client

    private val basketRepositoryDataSourceRemoteImpl = BasketRepositoryDataSourceRemoteImpl(client = client)

    /**
     * Добавление товрара в корзину.
     * При успешном результате эмитится объект типа ResponseModel.
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
    ): Flow<Result> = flow {
        try {
            basketRepositoryDataSourceRemoteImpl.addProductInBasketFlow(
                userId = userId,
                productId = productId,
                numberProducts = numberProducts
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

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

    /**
     * Удаление товара из коризины.
     * При успешном результате эмитится объект типа ResponseModel.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будет удален товар;
     * [productId] - идентификатор товара для удаления.
     */
    override fun deleteProductFromBasketFlow(
        userId: Int,
        productId: Int
    ): Flow<Result> = flow{
        try {
            basketRepositoryDataSourceRemoteImpl.deleteProductFromBasketFlow(
                userId = userId,
                productId = productId
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

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

    /**
     * Получение списка товаров из корзины пользователя.
     * При успешном результате эмитится список товаров из корзины ( List<BasketModel> ).
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getProductsFromBasketFlow(userId: Int): Flow<Result> = flow {
        try {
            basketRepositoryDataSourceRemoteImpl.getProductsFromBasketFlow(userId = userId)
                .collect{ resultDataSource ->
                    val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                    if (response != null) {
                        val _listBasketDataSourceModel = response.value as List<*>
                        val listBasketDataSourceModel = _listBasketDataSourceModel.map {
                            return@map it as BasketDataSourceModel
                        }

                        val listBasketModel = listBasketDataSourceModel.toListBasketModel()

                        val data = ResponseValueModel(
                            value = listBasketModel,
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

    /**
     * Получение списка идентификаторов товаров, находящихся в коризине пользователя.
     * При успешном результате эмитится список идентификаторов товаров из корзины ( List<Int> ).
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getIdsProductsFromBasketFlow(userId: Int): Flow<Result> = flow{
        try {
            basketRepositoryDataSourceRemoteImpl.getIdsProductsFromBasketFlow(userId = userId)
                .collect{ resultDataSource ->
                    val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                    if (response != null) {

                        val _listIdsProductsFromBasket = response.value as List<*>
                        val listIdsProductsFromBasket = _listIdsProductsFromBasket.map {
                            return@map it as Int
                        }

                        val data = ResponseValueModel(
                            value = listIdsProductsFromBasket,
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

    /**
     * Удаление нескольких товаров из коризины.
     * При успешном результате эмитится объект типа ResponseModel.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будут удалены товары;
     * [listIdsProducts] - список идентификаторов товаров для удаления.
     */
    override fun deleteProductsFromBasketFlow(
        userId: Int,
        listIdsProducts: List<Int>
    ): Flow<Result> = flow{
        try {
            basketRepositoryDataSourceRemoteImpl.deleteProductsFromBasketFlow(
                userId = userId,
                listIdsProducts = listIdsProducts
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null){
                    val data = response.toResponseModel()
                    emit(Result.Success(data = data))
                }
                else{
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

    /**
     * Обновление количество товара в корзине.
     * При успешном результате эмитится объект типа ResponseModel.
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
    ): Flow<Result> = flow{
        try {
            basketRepositoryDataSourceRemoteImpl.updateNumberProductsInBasketFlow(
                userId = userId,
                productId = productId,
                numberProducts = numberProducts
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

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

    /**
     * Обновление количества товаров в корзине.
     * При успешном результате эмитится успешный ответ (объект типа [ResponseModel]).
     *
     * Параметры:
     * [userId] - идентификатор пользователя;
     * [listNumberProductsModel] - список с новым количеством товаров.
     */
    override fun updateNumbersProductsInBasketFlow(
        userId: Int,
        listNumberProductsModel: List<NumberProductsModel>
    ): Flow<Result> = flow{
        try {
            val listNumberProductsDataSourceModel = listNumberProductsModel.toListNumberProductsDataSourceModel()
            basketRepositoryDataSourceRemoteImpl.updateNumbersProductsInBasketFlow(
                userId = userId,
                listNumberProductsDataSourceModel = listNumberProductsDataSourceModel
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

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

    /**
     * Преобразование [BasketDataSourceModel] в [BasketModel].
     */
    private fun BasketDataSourceModel.toBasketModel(): BasketModel {
        return BasketModel(
            productModel = this.productDataSourceModel.toProductModel(),
            numberProducts = this.numberProducts
        )
    }

    /**
     * Преобразование List<[BasketDataSourceModel]> в List<[BasketModel]>.
     */
    private fun List<BasketDataSourceModel>.toListBasketModel(): List<BasketModel> {
        val mutableListBasket = mutableListOf<BasketModel>()

        this.forEach { basketDataSourceModel ->
            mutableListBasket.add(basketDataSourceModel.toBasketModel())
        }

        return mutableListBasket
    }

}