package com.example.data.profile.datasource.remote

import android.util.Log
import com.example.data.profile.datasource.ErrorResultDataSource
import com.example.data.profile.datasource.ResultDataSource
import com.example.data.profile.datasource.SuccessResultDataSource
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
    ProfileRepositoryDataSourceRemote<ResponseDataSourceModel, ResponseValueDataSourceModel<UserDataSourceModel>,ResponseValueDataSourceModel<Int>> {

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

    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.113:$PORT"
        const val CREATE_USER_URL = "$BASE_URL/create/user"
        const val GET_USER_URL = "$BASE_URL/user"
        const val GET_USER_ID_URL = "$BASE_URL/user_id"
        const val GET_USER_BY_ID_URL = "$BASE_URL/user_by_id?id="
    }


}