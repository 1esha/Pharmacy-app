package com.example.data.orders

import android.util.Log
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.orders.datasource.OrdersRepositoryDataSourceRemoteImpl
import com.example.data.orders.datasource.models.OrderDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toListNumberProductsDataSourceModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.models.NumberProductsModel
import com.example.domain.orders.OrdersRepository
import com.example.domain.orders.models.OrderModel
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class OrdersRepositoryImpl: OrdersRepository {

    private val ordersRepositoryDataSourceRemoteImpl = OrdersRepositoryDataSourceRemoteImpl()

    /**
     * Создание заказа.
     * При успешном результате эмитится успешный ответ (объект типа [ResponseModel]).
     *
     * Параметры:
     * [userId] - идентификатор пользователя для которого оформляется заказ;
     * [addressId] - идентификатор аптеки, где будет получен заказ;
     * [listNumberProductsModel] - список количества товаров в заказе.
     */
    override fun createOrderFlow(
        userId: Int,
        addressId: Int,
        listNumberProductsModel: List<NumberProductsModel>
    ): Flow<Result> = flow{
        try {
            val listNumberProductsDataSourceModel = listNumberProductsModel.toListNumberProductsDataSourceModel()
            ordersRepositoryDataSourceRemoteImpl.createOrderFlow(
                userId = userId,
                addressId = addressId,
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
     * Получение списка истории покупок.
     * При успешном результате эмитится список заказов (List<[OrderModel]>).
     *
     * Параметры:
     * [userId] - идентификатор пользователя, чья история покупок будет получена.
     */
    override fun getPurchaseHistoryFlow(userId: Int): Flow<Result> = flow {
        try {
            ordersRepositoryDataSourceRemoteImpl.getPurchaseHistoryFlow(userId = userId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listOrderDataSourceModel = response.value as List<*>
                    val listOrderDataSourceModel = _listOrderDataSourceModel.map { it as OrderDataSourceModel }

                    val listOrderModel = listOrderDataSourceModel.toListOrderModel()

                    val data = ResponseValueModel(
                        value = listOrderModel,
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
     * Получение списка текущих заказов.
     * При успешном результате эмитится список заказов (List<[OrderModel]>).
     *
     * Параметры:
     * [userId] - идентификатор пользователя, чей списка текущих заказов будет получен.
     */
    override fun getCurrentOrdersFlow(userId: Int): Flow<Result> = flow {
        try {
            ordersRepositoryDataSourceRemoteImpl.getCurrentOrdersFlow(userId = userId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listOrderDataSourceModel = response.value as List<*>
                    val listOrderDataSourceModel = _listOrderDataSourceModel.map { it as OrderDataSourceModel }

                    val listOrderModel = listOrderDataSourceModel.toListOrderModel()

                    val data = ResponseValueModel(
                        value = listOrderModel,
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

    private fun List<OrderDataSourceModel>.toListOrderModel(): List<OrderModel>{
        val mutableListOrderModel = mutableListOf<OrderModel>()

        this.forEach { orderDataSourceModel ->
            mutableListOrderModel.add(orderDataSourceModel.toOrderModel())
        }

        return mutableListOrderModel
    }

    private fun OrderDataSourceModel.toOrderModel(): OrderModel{
        val one: Byte = 1
        val isActual = this.isActual == one
        return OrderModel(
            orderId = this.orderId,
            productId = this.productId,
            addressId = this.addressId,
            userId = this.userId,
            numberProduct = this.numberProduct,
            isActual = isActual,
            orderDate = this.orderDate.toDateTime()!!,
            endDate = this.endDate.toDateTime()
        )
    }

    private fun String?.toDateTime(): String? {
        if (this == null) return null

        val dateTime = this.substringBefore('.')
        var result = ""
        dateTime.forEach { char ->
            result += if (char == 'T') ' ' else char
        }

        return result
    }

}