package com.example.data.advertisements.datasource

import com.example.data.ResultDataSource
import kotlinx.coroutines.flow.Flow

interface AdvertisementRepositoryDataSourceRemote {

    fun getHomeAdvertisement(): Flow<ResultDataSource>

    fun getRecommendedProducts(): Flow<ResultDataSource>

}