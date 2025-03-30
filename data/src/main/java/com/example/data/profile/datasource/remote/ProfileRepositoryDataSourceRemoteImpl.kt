package com.example.data.profile.datasource.remote

import android.util.Log
import com.example.data.ResultDataSource
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
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
 * Класс [ProfileRepositoryDataSourceRemoteImpl] является реализацией интерфейса [ProfileRepositoryDataSourceRemote].
 */
class ProfileRepositoryDataSourceRemoteImpl : ProfileRepositoryDataSourceRemote {

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
     * Создание нового пользователя.
     *
     * Параметры:
     * [userInfoDataSourceModel] - информация пользователя.
     */
    override fun createUserFlow(userInfoDataSourceModel: UserInfoDataSourceModel): Flow<ResultDataSource> = flow{
        Log.d("TAG","createUserFlow")
        try {
            val response = client.request {
                url(CREATE_USER_URL)
                method = HttpMethod.Post
                setBody(userInfoDataSourceModel)
                contentType(ContentType.Application.Json)
            }

            val data = response.body<ResponseDataSourceModel>()

            if (
                data.status in 200..299
            ) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение данных пользователя по логину и паролю.
     *
     * Параметры:
     * [logInDataSourceModel] - модель данных с логином и паролем.
     */
    override fun getUserFlow(logInDataSourceModel: LogInDataSourceModel): Flow<ResultDataSource> = flow{
        Log.d("TAG","getUseFlow")
        try {
            val response = client.request {
                url(GET_USER_URL)
                method = HttpMethod.Post
                setBody(logInDataSourceModel)
                contentType(ContentType.Application.Json)
            }
            val data = response.body<ResponseValueDataSourceModel<UserDataSourceModel>>()

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
     * Получение идентификатора пользователя по информации пользователя.
     *
     * Параметры:
     * [userInfoDataSourceModel] - информация пользователя.
     */
    override fun getUserIdFlow(userInfoDataSourceModel: UserInfoDataSourceModel): Flow<ResultDataSource> = flow{
        Log.d("TAG","getUserIdFlow")
        try {
            val response = client.request {
                url(GET_USER_ID_URL)
                method = HttpMethod.Post
                setBody(userInfoDataSourceModel)
                contentType(ContentType.Application.Json)
            }
            val data = response.body<ResponseValueDataSourceModel<Int>>()
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
     * Получение данных пользователя по его идентификатору.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getUserByIdFlow(userId: Int): Flow<ResultDataSource> = flow{
        Log.d("TAG","getUserByIdFlow")
        try {
            val response = client.request {
                url(GET_USER_BY_ID_URL)
                parameter("id",userId)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseValueDataSourceModel<UserDataSourceModel>>()

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
     * Редактирование данных пользователя.
     *
     * Параметры:
     * [userDataSourceModel] - новые данные пользователя.
     */
    override fun editUserFlow(userDataSourceModel: UserDataSourceModel): Flow<ResultDataSource> = flow{
        Log.d("TAG","editUserFlow")
        try {
            val response = client.request {
                url(EDIT_USER_URL)
                method = HttpMethod.Post
                setBody(userDataSourceModel)
                contentType(ContentType.Application.Json)
            }
            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление аккаунта пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя аккаунт которого будет удален.
     */
    override fun deleteUserFlow(userId: Int): Flow<ResultDataSource> = flow{
        Log.d("TAG","deleteUserFlow")
        try {
            val response = client.request {
                url(DELETE_USER_URL)
                parameter("id",userId)
                method = HttpMethod.Get
            }
            val data = response.body<ResponseDataSourceModel>()

            if (data.status in 200..299) {
                val result = ResultDataSource.Success(data = data)
                emit(result)
            }
            else {
                emit(ResultDataSource.Error(exception = ServerException(serverMessage = data.message)))
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение города, который выбрал пользователь.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getCityByUserIdFlow(userId: Int): Flow<ResultDataSource> = flow{
        Log.d("TAG","getCityByUserIdFlow")
        try {
            // если пользователь не авторизован
            if (userId < 0) {
                val data = ResponseValueDataSourceModel(
                    value = NOT_SELECTED,
                    responseDataSourceModel = ResponseDataSourceModel(
                        message = SUCCESS,
                        status = SUCCESS_CODE
                    )
                )
                emit(ResultDataSource.Success(data))
            }
            else{
                val response = client.request {
                    url(GET_CITY_BY_USER_ID)
                    parameter("id",userId)
                    method = HttpMethod.Get
                }
                val _data = response.body<ResponseValueDataSourceModel<Map<String,String>>>()

                val map = _data.value
                val city = map?.values?.first()

                if (
                    _data.responseDataSourceModel.status in 200..299 &&
                    city != null
                ) {
                    val data = ResponseValueDataSourceModel(
                        value = city,
                        responseDataSourceModel = _data.responseDataSourceModel
                    )
                    val result = ResultDataSource.Success(data = data)
                    emit(result)
                }
                else {
                    emit(ResultDataSource.Error(exception = ServerException(serverMessage = _data.responseDataSourceModel.message)))
                }
            }
        }
        catch (e: Exception){
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    companion object {

        const val SUCCESS = "Успешно"
        const val SUCCESS_CODE = 200
        const val NOT_SELECTED = "NOT_SELECTED"

        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
        const val CREATE_USER_URL = "/create/user"
        const val GET_USER_URL = "/user"
        const val GET_USER_ID_URL = "/user_id"
        const val GET_USER_BY_ID_URL = "/user_by_id"
        const val EDIT_USER_URL ="/user/edit"
        const val DELETE_USER_URL = "/user/delete"
        const val GET_CITY_BY_USER_ID = "/city/user_id"
    }


}