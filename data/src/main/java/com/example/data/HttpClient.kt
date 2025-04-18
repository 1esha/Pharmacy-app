package com.example.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.gson.gson

class HttpClient {

    val client = HttpClient(OkHttp) {
        // URL запроса по умолчанию
        defaultRequest {
            url(BASE_URL)
        }
        install(ContentNegotiation) {
            gson()
        }
    }

    companion object {
        private const val PORT = "4000"
        private const val BASE_URL = "http://192.168.0.114:$PORT"
    }

}