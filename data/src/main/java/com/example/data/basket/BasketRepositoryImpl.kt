package com.example.data.basket

import com.example.data.asSuccessResultDataSource
import com.example.data.basket.datasource.BasketRepositoryDataSourceRemoteImpl
import com.example.data.basket.datasource.models.BasketDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toProductModel
import com.example.data.toResponseModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.basket.BasketRepository
import com.example.domain.basket.models.BasketModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel

/**
 * Класс [BasketRepositoryImpl] является реализвцией интерфейса [BasketRepository].
 */
class BasketRepositoryImpl: BasketRepository<
        ResponseModel,
        ResponseValueModel<List<BasketModel>>,
        ResponseValueModel<List<Int>>
        > {

    private val basketRepositoryDataSourceRemoteImpl = BasketRepositoryDataSourceRemoteImpl()

    /**
     * Добавление товрара в корзину.
     *
     * Параметры:
     * [userId] - идентификатор пользователя в чью корзину будет добавлен товар;
     * [productId] - идентификатор товара для добавления;
     * [numberProducts] - количество товра, которое будет добавлено в коризну.
     */
    override suspend fun addProductInBasket(userId: Int, productId: Int, numberProducts: Int): Result<ResponseModel> {

        val responseDataSourceModel = basketRepositoryDataSourceRemoteImpl.addProductInBasket(
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )
        val valueDataSource = responseDataSourceModel.asSuccessResultDataSource()?.value
        val value = valueDataSource?.toResponseModel() ?: defaultResponseModel
        val result = responseDataSourceModel.toResult(
            value = value
        )

        return result
    }

    /**
     * Удаление товара из коризины.
     *
     * Параметры:
     * [userId] - идентификатор пользователя из чьей коризины будет удален товар;
     * [productId] - идентификатор товара для удаления.
     */
    override suspend fun deleteProductFromBasket(userId: Int, productId: Int): Result<ResponseModel> {

        val responseDataSourceModel = basketRepositoryDataSourceRemoteImpl.deleteProductFromBasket(
            userId = userId,
            productId = productId
        )
        val valueDataSource = responseDataSourceModel.asSuccessResultDataSource()?.value
        val value = valueDataSource?.toResponseModel() ?: defaultResponseModel
        val result = responseDataSourceModel.toResult(
            value = value
        )

        return result
    }

    /**
     * Получение списка идентификаторов товаров, находящихся в коризине пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override suspend fun getIdsProductsFromBasket(userId: Int): Result<ResponseValueModel<List<Int>>> {

        val responseValueDataSourceModel = basketRepositoryDataSourceRemoteImpl.getIdsProductsFromBasket(
            userId = userId
        )

        val valueDataSource = responseValueDataSourceModel.asSuccessResultDataSource()?.value
        val listIdsProductsFromBasket = valueDataSource?.value
        val value = valueDataSource?.toResponseValueModel(value = listIdsProductsFromBasket)

        val result = responseValueDataSourceModel.toResult(
            value = value
        )

        return result
    }

    /**
     * Получение списка товаров из корины пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override suspend fun getProductsFromBasket(userId: Int): Result<ResponseValueModel<List<BasketModel>>> {

        val responseDataSourceModel = basketRepositoryDataSourceRemoteImpl.getProductsFromBasket(
            userId = userId
        )

        val valueDataSource = responseDataSourceModel.asSuccessResultDataSource()?.value

        val listProductDataSourceModel = valueDataSource?.value

        val value = valueDataSource?.toResponseValueModel(
            value = listProductDataSourceModel?.toListBasketModel()
        )

        val result = responseDataSourceModel.toResult(
            value = value
        )

        return result
    }

    /**
     * Обновление количество товара в корзине.
     *
     * Параметры:
     * [userId] - идентификатор пользователя;
     * [productId] - идентификатор товара;
     * [numberProducts] - новое количество товара в корзине.
     */
    override suspend fun updateNumberProductsInBasket(userId: Int, productId: Int, numberProducts: Int): Result<ResponseModel> {

        val responseDataSourceModel = basketRepositoryDataSourceRemoteImpl.updateNumberProductsInBasket(
            userId = userId,
            productId = productId,
            numberProducts = numberProducts
        )
        val valueDataSource = responseDataSourceModel.asSuccessResultDataSource()?.value
        val value = valueDataSource?.toResponseModel() ?: defaultResponseModel
        val result = responseDataSourceModel.toResult(
            value = value
        )

        return result
    }

    /**
     * Преобразование [ResponseValueDataSourceModel] в [ResponseValueModel].
     *
     * Параметры:
     * [value] - значение для [ResponseValueModel].
     */
    private fun <I,O>ResponseValueDataSourceModel<I>.toResponseValueModel(value: O?): ResponseValueModel<O> {
        return ResponseValueModel(
            value = value,
            responseModel = this.responseDataSourceModel.toResponseModel()
        )
    }

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

    companion object {
        val defaultResponseModel = ResponseModel(
            message = "Ошибка",
            status = 404
        )
    }


}