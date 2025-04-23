package com.example.domain.advertisements

import com.example.domain.Result
import kotlinx.coroutines.flow.Flow

interface AdvertisementRepository {

    fun getHomeAdvertisement(): Flow<Result>

    fun getRecommendedProducts(): Flow<Result>
}