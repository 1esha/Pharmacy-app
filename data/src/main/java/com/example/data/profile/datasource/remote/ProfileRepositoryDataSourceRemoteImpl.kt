package com.example.data.profile.datasource.remote

import android.util.Log
import com.example.data.ErrorResultDataSource
import com.example.data.ResultDataSource
import com.example.data.SuccessResultDataSource
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepositoryDataSourceRemoteImpl :
    ProfileRepositoryDataSourceRemote<
            ResponseDataSourceModel,
            ResponseValueDataSourceModel<UserDataSourceModel>,
            ResponseValueDataSourceModel<Int>,
            ResponseValueDataSourceModel<String>> {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            gson()
        }
    }

    override suspend fun createUser(userInfoDataSourceModel: UserInfoDataSourceModel): ResultDataSource<ResponseDataSourceModel> =
        withContext(Dispatchers.IO) {
            try {
                Log.i("TAG", "userInfoDataSourceModel = $userInfoDataSourceModel")
                val response = client.request {
                    url(CREATE_USER_URL)
                    method = HttpMethod.Post
                    setBody(userInfoDataSourceModel)
                    contentType(ContentType.Application.Json)
                }
                val responseDataSourceModel = response.body<ResponseDataSourceModel>()

                val successResultDataSource = SuccessResultDataSource(
                    value = responseDataSourceModel
                )
                Log.i("TAG", "createUser successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                    exception = e
                )
                Log.i("TAG", "createUser errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getUser(logInDataSourceModel: LogInDataSourceModel): ResultDataSource<ResponseValueDataSourceModel<UserDataSourceModel>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_USER_URL)
                    method = HttpMethod.Post
                    setBody(logInDataSourceModel)
                    contentType(ContentType.Application.Json)
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<UserDataSourceModel>>()

                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG", "getUser successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<UserDataSourceModel>>(
                    exception = e
                )
                Log.i("TAG", "getUser errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getUserId(userInfoDataSourceModel: UserInfoDataSourceModel): ResultDataSource<ResponseValueDataSourceModel<Int>> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(GET_USER_ID_URL)
                    method = HttpMethod.Post
                    setBody(userInfoDataSourceModel)
                    contentType(ContentType.Application.Json)
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<Int>>()

                Log.i("TAG", "getUserId responseValueDataSourceModel ${responseValueDataSourceModel.value}")

                val successResultDataSource = SuccessResultDataSource(
                    value = responseValueDataSourceModel
                )
                Log.i("TAG", "getUserId successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<Int>>(
                    exception = e
                )
                Log.i("TAG", "getUserId errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getUserById(userId: Int): ResultDataSource<ResponseValueDataSourceModel<UserDataSourceModel>> =
    withContext(Dispatchers.IO) {
        try {
            val response = client.request {
                url(GET_USER_BY_ID_URL+"$userId")
                method = HttpMethod.Get
            }
            val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<UserDataSourceModel>>()

            val successResultDataSource = SuccessResultDataSource(
                value = responseValueDataSourceModel
            )
            Log.i("TAG", "getUserById successResultDataSource ${successResultDataSource.value}")
            return@withContext successResultDataSource
        } catch (e: Exception) {
            val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<UserDataSourceModel>>(
                exception = e
            )
            Log.i("TAG", "getUserById errorResultDataSource ${errorResultDataSource.exception}")
            return@withContext errorResultDataSource
        }
    }

    override suspend fun editUser(userDataSourceModel: UserDataSourceModel): ResultDataSource<ResponseDataSourceModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(EDIT_USER_URL)
                    method = HttpMethod.Post
                    setBody(userDataSourceModel)
                    contentType(ContentType.Application.Json)
                }
                val responseDataSourceModel = response.body<ResponseDataSourceModel>()

                val successResultDataSource = SuccessResultDataSource(
                    value = responseDataSourceModel
                )
                Log.i("TAG", "editUser successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                    exception = e
                )
                Log.i("TAG", "editUser errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun deleteUser(userId: Int): ResultDataSource<ResponseDataSourceModel> =
        withContext(Dispatchers.IO) {
            try {
                val response = client.request {
                    url(DELETE_USER_URL+"$userId")
                    method = HttpMethod.Get
                }
                val responseDataSourceModel = response.body<ResponseDataSourceModel>()

                val successResultDataSource = SuccessResultDataSource(
                    value = responseDataSourceModel
                )
                Log.i("TAG", "deleteUser successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                    exception = e
                )
                Log.i("TAG", "deleteUser errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    override suspend fun getCityByUserId(userId: Int): ResultDataSource<ResponseValueDataSourceModel<String>> =
        withContext(Dispatchers.IO) {
            try {
                // если пользователь не авторизован
                if (userId <= 0) {
                    return@withContext SuccessResultDataSource(
                        value = ResponseValueDataSourceModel(
                            value = NOT_SELECTED,
                            responseDataSourceModel = ResponseDataSourceModel(
                                message = SUCCESS,
                                status = SUCCESS_CODE
                            )
                        )
                    )
                }
                val response = client.request {
                    url(GET_CITY_BY_USER_ID+"$userId")
                    method = HttpMethod.Get
                }
                val responseValueDataSourceModel = response.body<ResponseValueDataSourceModel<Map<String,String>>>()

                val city = responseValueDataSourceModel.value?.values?.first() ?:
                throw NullPointerException("getCityByUserId city = null")

                val successResultDataSource = SuccessResultDataSource(
                    value = ResponseValueDataSourceModel(
                        value = city,
                        responseDataSourceModel = responseValueDataSourceModel.responseDataSourceModel
                    )
                )
                Log.i("TAG", "getCityByUserId successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseValueDataSourceModel<String>>(
                    exception = e
                )
                Log.i("TAG", "getCityByUserId errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }

    companion object {

        const val SUCCESS = "Успешно"
        const val SUCCESS_CODE = 200
        const val NOT_SELECTED = "NOT_SELECTED"

        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
        const val CREATE_USER_URL = "$BASE_URL/create/user"
        const val GET_USER_URL = "$BASE_URL/user"
        const val GET_USER_ID_URL = "$BASE_URL/user_id"
        const val GET_USER_BY_ID_URL = "$BASE_URL/user_by_id?id="
        const val EDIT_USER_URL ="$BASE_URL/user/edit"
        const val DELETE_USER_URL = "$BASE_URL/user/delete?id="
        const val GET_CITY_BY_USER_ID = "$BASE_URL/city/user_id?id="
    }


}