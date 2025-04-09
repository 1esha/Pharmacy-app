package com.example.data.orders

import android.util.Log
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.orders.datasource.OrdersRepositoryDataSourceRemoteImpl
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.toListNumberProductsDataSourceModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.models.NumberProductsModel
import com.example.domain.orders.OrdersRepository
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

}