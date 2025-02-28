package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.models.ResponseValueModel


class GetOperatingModeUseCase(
    private val catalogRepository: CatalogRepository<*, *, *, *, *, ResponseValueModel<List<OperatingModeModel>?>>
) {

    suspend fun execute(): Result<ResponseValueModel<List<OperatingModeModel>?>> {
        val result = catalogRepository.getOperatingMode()

        return result
    }

}