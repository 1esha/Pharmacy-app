package com.example.domain.catalog

import com.example.domain.Result

interface CatalogRepository<T> {

    suspend fun getAllProducts(): Result<T>

    suspend fun getProductsByPath(path: String): Result<T>
}