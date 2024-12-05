package com.example.data.profile.datasource.remote

import android.util.Log
import com.example.data.profile.datasource.ErrorResultDataSource
import com.example.data.profile.datasource.ResultDataSource
import com.example.data.profile.datasource.SuccessResultDataSource
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
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

class ProfileRepositoryDataSourceRemoteImpl<T> :
    ProfileRepositoryDataSourceRemote<ResponseDataSourceModel, ResponseValueDataSourceModel<T>> {

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
                Log.i("TAG", "successResultDataSource ${successResultDataSource.value}")
                return@withContext successResultDataSource
            } catch (e: Exception) {
                val errorResultDataSource = ErrorResultDataSource<ResponseDataSourceModel>(
                    exception = e
                )
                Log.i("TAG", "errorResultDataSource ${errorResultDataSource.exception}")
                return@withContext errorResultDataSource
            }
        }


    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.113:$PORT"
        const val CREATE_USER_URL = "$BASE_URL/create/user"
    }


}