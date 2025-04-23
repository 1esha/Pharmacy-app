package com.example.domain.advertisements.usecases

import com.example.domain.Result
import com.example.domain.advertisements.AdvertisementRepository
import kotlinx.coroutines.flow.Flow

class GetRecommendedProductsUseCase(
    private val advertisementRepository: AdvertisementRepository
) {

    fun execute(): Flow<Result> {
        val result = advertisementRepository.getRecommendedProducts()
        return result
    }

}