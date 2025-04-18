package com.example.data.catalog.datasource.remote

import android.util.Log
import com.example.data.ResultDataSource
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import com.example.data.catalog.datasource.models.AddressIdAndIdsProductsDataSourceModel
import com.example.data.catalog.datasource.models.IdsProductsDataSourceModel
import com.example.data.catalog.datasource.models.OperatingModeDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDetailsDataSourceModel
import com.example.data.catalog.datasource.models.ProductAvailabilityDataSourceModel
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.domain.ServerException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class CatalogRepositoryDataSourceRemoteImpl(
    private val client: HttpClient
): CatalogRepositoryDataSourceRemote{

    /**
     * Получение всех товаров..
     */
    override fun getAllProductsFlow(): Flow<ResultDataSource> = flow{
        Log.d("TAG","getAllProductsFlow")
        try {
            val response = client.request {
                url(GET_ALL_PRODUCTS_URL)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
                ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }

    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка товаров по переданному пути.
     *
     * Параметры:
     * [path] - путь по которому будет получен список товаров.
     */
    override fun getProductsByPathFlow(path: String): Flow<ResultDataSource> = flow{
        Log.d("TAG","getProductsByPathFlow")
        try {
            val response = client.request {
                url(GET_PRODUCTS_BY_PATH)
                parameter("path",path)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение товара по его идентификатору.
     *
     * Параметры:
     * [productId] - идентификатор получаемого товара.
     */
    override fun getProductByIdFlow(productId: Int): Flow<ResultDataSource> = flow{
        Log.d("TAG","getProductByIdFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_BY_ID)
                parameter("id",productId)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<ProductDataSourceModel>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение товаров по их идентификаторам.
     *
     * Параметры:
     * [listIdsProducts] - список идентификаторов по которому будет получен список товаров.
     */
    override fun getProductsByIdsFlow(listIdsProducts: List<Int>): Flow<ResultDataSource> = flow{
        Log.d("TAG", "getProductsByIdsFlow")
        try {
            val response = client.request {
                url(GET_PRODUCTS_BY_IDS)
                method = HttpMethod.Post
                setBody(IdsProductsDataSourceModel(listIdsProducts = listIdsProducts))
                contentType(type = ContentType.Application.Json)
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
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
     * Получение товаров с помощью поиска.
     *
     * Параметры:
     * [searchText] - текст поиска.
     */
    override fun getProductsBySearchFlow(searchText: String): Flow<ResultDataSource>  = flow{
        Log.d("TAG","getProductsBySearchFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_BY_SEARCH)
                parameter("search",searchText)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseValueDataSourceModel<List<ProductDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка данных о аптеках.
     */
    override fun getPharmacyAddressesFlow(): Flow<ResultDataSource> = flow{
        Log.d("TAG","getPharmacyAddressesFlow")
        try {
            val response = client.request {
                url(GET_PHARMACY_ADDRESSES)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка подробной информации о аптеках.
     */
    override fun getPharmacyAddressesDetailsFlow(): Flow<ResultDataSource> = flow{
        Log.d("TAG","getPharmacyAddressesDetailsFlow")
        try {
            val response = client.request {
                url(GET_PHARMACY_ADDRESSES_DETAILS)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseValueDataSourceModel<List<PharmacyAddressesDetailsDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка наличия товаров по переданному пути.
     *
     * Параметры:
     * [path] - путь по которому будет получен список данных о наличии товаров в аптеках.
     */
    override fun getProductAvailabilityByPathFlow(path: String): Flow<ResultDataSource> = flow{
        Log.d("TAG","getProductAvailabilityByPathFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_AVAILABILITY_BY_PATH)
                parameter("path",path)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка наличия товара в аптеках.
     *
     * Параметры:
     * [productId] - идентификатор товара наличие которого будет получено.
     */
    override fun getProductAvailabilityByProductIdFlow(productId: Int): Flow<ResultDataSource> = flow{
        Log.d("TAG","getProductAvailabilityByProductIdFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID)
                parameter("id",productId)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка наличия товаров в текущей аптеке.
     *
     * Параметры:
     * [addressId] - идентификатор аптеки из которой будет получен список наличия товаров;
     * [listIdsProducts] - список идентификаторов товаров.
     */
    override fun getProductAvailabilityByAddressIdFlow(
        addressId: Int,
        listIdsProducts: List<Int>
    ): Flow<ResultDataSource> = flow{
        Log.d("TAG", "getProductAvailabilityByAddressIdFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID)
                method = HttpMethod.Post
                Log.d("TAG", "AddressIdAndIdsProductsDataSourceModel ${   AddressIdAndIdsProductsDataSourceModel(addressId = addressId, listIdsProducts = listIdsProducts)}")
                setBody(AddressIdAndIdsProductsDataSourceModel(addressId = addressId, listIdsProducts = listIdsProducts))
                contentType(type = ContentType.Application.Json)
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
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
     * Получение списка наличия товаров по списку идентификаторов.
     *
     * Параметры:
     * [listIdsProducts] - список идентификаторов по которому будет получен список данных о наличии товаров в аптеках.
     */
    override fun getProductAvailabilityByIdsProductsFlow(listIdsProducts: List<Int>): Flow<ResultDataSource> = flow{
        Log.d("TAG", "getProductAvailabilityByIdsProductsFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS)
                method = HttpMethod.Post
                setBody(IdsProductsDataSourceModel(listIdsProducts = listIdsProducts))
                contentType(type = ContentType.Application.Json)
            }

            val data = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
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
     * Получение списка наличия товаров в аптеках.
     */
    override fun getProductAvailabilityFlow(): Flow<ResultDataSource> = flow{
        Log.d("TAG","getProductAvailabilityFlow")
        try {
            val response = client.request {
                url(GET_PRODUCT_AVAILABILITY)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение списка с режимами работы аптек.
     */
    override fun getOperatingModeFlow(): Flow<ResultDataSource> = flow{
        Log.d("TAG","getOperatingModeFlow")
        try {
            val response = client.request {
                url(GET_OPERATING_MODE)
                method = HttpMethod.Get
            }

            val data = response.body<ResponseValueDataSourceModel<List<OperatingModeDataSourceModel>>>()

            if (
                data.responseDataSourceModel.status in 200..299 &&
                data.value != null
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.responseDataSourceModel.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Обновление количества товаров в аптеке.
     *
     * Параметры:
     * [addressId] - идентификатор аптеки;
     * [listNumberProductsDataSourceModel] - список с новым количеством товаров.
     */
    override fun updateNumbersProductsInPharmacyFlow(
        addressId: Int,
        listNumberProductsDataSourceModel: List<NumberProductsDataSourceModel>
    ): Flow<ResultDataSource> = flow {
        Log.d("TAG", "updateNumbersProductsInPharmacyFlow")
        try {
            val response = client.request {
                url(UPDATE_NUMBER_PRODUCTS)
                method = HttpMethod.Post
                setBody(
                    object {
                        val addressId = addressId
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
        const val GET_ALL_PRODUCTS_URL = "/products"
        const val GET_PRODUCTS_BY_PATH = "/products/path"
        const val GET_PHARMACY_ADDRESSES = "/pharmacy/addresses"
        const val GET_PHARMACY_ADDRESSES_DETAILS = "/pharmacy/addresses_details"
        const val GET_PRODUCT_AVAILABILITY_BY_PATH = "/availability"
        const val GET_PRODUCT_BY_ID = "/product/id"
        const val GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID = "/availability/product_id"
        const val GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS = "/availability/ids_products"
        const val GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID = "/availability/address_id"
        const val GET_OPERATING_MODE = "/operating_mode"
        const val GET_PRODUCT_AVAILABILITY = "/availability/all"
        const val GET_PRODUCTS_BY_IDS = "/products/ids_products"
        const val UPDATE_NUMBER_PRODUCTS = "/products/update_number_products"
        const val GET_PRODUCT_BY_SEARCH = "/products/search"
    }
}