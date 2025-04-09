package com.example.data.catalog

import android.util.Log
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.catalog.datasource.models.OperatingModeDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDetailsDataSourceModel
import com.example.data.catalog.datasource.models.ProductAvailabilityDataSourceModel
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.catalog.datasource.remote.CatalogRepositoryDataSourceRemoteImpl
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toListNumberProductsDataSourceModel
import com.example.data.toListOperatingModeModel
import com.example.data.toListPharmacyAddressesDetailsModel
import com.example.data.toListPharmacyAddressesModel
import com.example.data.toListProductAvailabilityModel
import com.example.data.toListProductModel
import com.example.data.toProductModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.models.NumberProductsModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class CatalogRepositoryImpl() : CatalogRepository{

    private val catalogRepositoryDataSourceRemoteImpl = CatalogRepositoryDataSourceRemoteImpl()

    /**
     * Получение всех товаров.
     * При успешном результате эмитится список всех товаров ( List<[ProductModel]> ).
     */
    override fun getAllProductsFlow(): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getAllProductsFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductDataSourceModel = response.value as List<*>
                    val listProductDataSourceModel = _listProductDataSourceModel.map { it as ProductDataSourceModel }

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

    /**
     * Получение списка товаров по переданному пути.
     * При успешном результате эмитится список товаров ( List<[ProductModel]> ).
     *
     * Параметры:
     * [path] - путь по которому будет получен список товаров.
     */
    override fun getProductsByPathFlow(path: String): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductsByPathFlow(path = path).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductDataSourceModel = response.value as List<*>
                    val listProductDataSourceModel = _listProductDataSourceModel.map { it as ProductDataSourceModel }

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

    /**
     * Получение товара по его идентификатору.
     * При успешном результате эмитится модель товара ( ProductModel ).
     *
     * Параметры:
     * [productId] - идентификатор получаемого товара.
     */
    override fun getProductByIdFlow(productId: Int): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductByIdFlow(productId = productId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val productDataSourceModel = response.value as ProductDataSourceModel
                    val productModel = productDataSourceModel.toProductModel()

                    val data = ResponseValueModel(
                        value = productModel,
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
     * Получение товаров по их идентификаторам.
     * При успешном результате эмитится список товаров ( List<[ProductModel]> ).
     *
     * Параметры:
     * [listIdsProducts] - список идентификаторов по которому будет получен список товаров.
     */
    override fun getProductsByIdsFlow(listIdsProducts: List<Int>): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductsByIdsFlow(listIdsProducts = listIdsProducts).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductDataSourceModel = response.value as List<*>
                    val listProductDataSourceModel = _listProductDataSourceModel.map { it as ProductDataSourceModel }
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

    /**
     * Получение списка данных о аптеках.
     * При успешном результате эмитится список данных о аптеках ( List<[PharmacyAddressesModel]> ).
     */
    override fun getPharmacyAddressesFlow(): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getPharmacyAddressesFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listPharmacyAddressesDataSourceModel = response.value as List<*>
                    val listPharmacyAddressesDataSourceModel = _listPharmacyAddressesDataSourceModel.map { it as PharmacyAddressesDataSourceModel }

                    val listPharmacyAddressesModel = listPharmacyAddressesDataSourceModel.toListPharmacyAddressesModel()

                    val data = ResponseValueModel(
                        value = listPharmacyAddressesModel,
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
     * Получение списка подробной информации о аптеках.
     * При успешном результате эмитится список подробной информации о аптеках ( List<[PharmacyAddressesDetailsModel]> ).
     */
    override fun getPharmacyAddressesDetailsFlow(): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getPharmacyAddressesDetailsFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listPharmacyAddressesDetailsDataSourceModel = response.value as List<*>
                    val listPharmacyAddressesDetailsDataSourceModel = _listPharmacyAddressesDetailsDataSourceModel.map { it as PharmacyAddressesDetailsDataSourceModel }

                    val listPharmacyAddressesDetailsModel = listPharmacyAddressesDetailsDataSourceModel.toListPharmacyAddressesDetailsModel()

                    val data = ResponseValueModel(
                        value = listPharmacyAddressesDetailsModel,
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
     * Получение списка наличия товаров по переданному пути.
     * При успешном результате эмитится список данных о наличии товаров в аптеках ( List<[ProductAvailabilityModel]> ).
     *
     * Параметры:
     * [path] - путь по которому будет получен список данных о наличии товаров в аптеках.
     */
    override fun getProductAvailabilityByPathFlow(path: String): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByPathFlow(path = path).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductAvailabilityDataSourceModel = response.value as List<*>
                    val listProductAvailabilityDataSourceModel = _listProductAvailabilityDataSourceModel.map { it as ProductAvailabilityDataSourceModel }

                    val listProductAvailabilityModel = listProductAvailabilityDataSourceModel.toListProductAvailabilityModel()

                    val data = ResponseValueModel(
                        value = listProductAvailabilityModel,
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
     * Получение списка наличия товара в аптеках по идентификатору товара.
     * При успешном результате эмитится список данных о наличии товара в аптеках ( List<[ProductAvailabilityModel]> ).
     *
     * Параметры:
     * [productId] - идентификатор товара наличие которого будет получено.
     */
    override fun getProductAvailabilityByProductIdFlow(productId: Int): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByProductIdFlow(productId = productId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductAvailabilityDataSourceModel = response.value as List<*>
                    val listProductAvailabilityDataSourceModel = _listProductAvailabilityDataSourceModel.map { it as ProductAvailabilityDataSourceModel }
                    val listProductAvailabilityModel = listProductAvailabilityDataSourceModel.toListProductAvailabilityModel()

                    val data = ResponseValueModel(
                        value = listProductAvailabilityModel,
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
     * Получение списка наличия товаров в текущей аптеке.
     * При успешном результате эмитится список наличия товаров о аптеках ( List<[ProductAvailabilityModel]> ).
     *
     * Параметры:
     * [addressId] - идентификатор аптеки из которой будет получен список наличия товаров;
     * [listIdsProducts] - список идентификаторов товаров.
     */
    override fun getProductAvailabilityByAddressIdFlow(
        addressId: Int,
        listIdsProducts: List<Int>
    ): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByAddressIdFlow(
                addressId = addressId,
                listIdsProducts = listIdsProducts
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductAvailabilityDataSourceModel = response.value as List<*>
                    val listProductAvailabilityDataSourceModel = _listProductAvailabilityDataSourceModel.map { it as ProductAvailabilityDataSourceModel }
                    val listProductAvailabilityModel = listProductAvailabilityDataSourceModel.toListProductAvailabilityModel()

                    val data = ResponseValueModel(
                        value = listProductAvailabilityModel,
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
     * Получение списка наличия товаров по списку идентификаторов.
     * При успешном результате эмитится список наличия товаров о аптеках ( List<[ProductAvailabilityModel]> ).
     *
     * Параметры:
     * [listIdsProducts] - список идентификаторов по которому будет получен список данных о наличии товаров в аптеках.
     */
    override fun getProductAvailabilityByIdsProductsFlow(listIdsProducts: List<Int>): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByIdsProductsFlow(listIdsProducts = listIdsProducts).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductAvailabilityDataSourceModel = response.value as List<*>
                    val listProductAvailabilityDataSourceModel = _listProductAvailabilityDataSourceModel.map { it as ProductAvailabilityDataSourceModel }
                    val listProductAvailabilityModel = listProductAvailabilityDataSourceModel.toListProductAvailabilityModel()

                    val data = ResponseValueModel(
                        value = listProductAvailabilityModel,
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
     * Получение списка наличия товаров в аптеках.
     * При успешном результате эмитится список наличия товаров в аптеках ( List<[ProductAvailabilityModel]> ).
     */
    override fun getProductAvailabilityFlow(): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listProductAvailabilityDataSourceModel = response.value as List<*>
                    val listProductAvailabilityDataSourceModel = _listProductAvailabilityDataSourceModel.map { it as ProductAvailabilityDataSourceModel }

                    val listProductAvailabilityModel = listProductAvailabilityDataSourceModel.toListProductAvailabilityModel()

                    val data = ResponseValueModel(
                        value = listProductAvailabilityModel,
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
     * Получение списка с режимами работы аптек.
     * При успешном результате эмитится список режимов работы аптек ( List<[OperatingModeModel]> ).
     */
    override fun getOperatingModeFlow(): Flow<Result> = flow {
        try {
            catalogRepositoryDataSourceRemoteImpl.getOperatingModeFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val _listOperatingModeDataSourceModel = response.value as List<*>
                    val listOperatingModeDataSourceModel = _listOperatingModeDataSourceModel.map { it as OperatingModeDataSourceModel }

                    val listOperatingModeModel = listOperatingModeDataSourceModel.toListOperatingModeModel()

                    val data = ResponseValueModel(
                        value = listOperatingModeModel,
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
     * Обновление количества товаров в аптеке.
     * При успешном результате эмитится успешный ответ (объект типа [ResponseModel]).
     *
     * Параметры:
     * [addressId] - идентификатор аптеки;
     * [listNumberProductsModel] - список с новым количеством товаров.
     */
    override fun updateNumbersProductsInPharmacyFlow(
        addressId: Int,
        listNumberProductsModel: List<NumberProductsModel>
    ): Flow<Result> = flow{
        try {
            val listNumberProductsDataSourceModel = listNumberProductsModel.toListNumberProductsDataSourceModel()
            catalogRepositoryDataSourceRemoteImpl.updateNumbersProductsInPharmacyFlow(
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