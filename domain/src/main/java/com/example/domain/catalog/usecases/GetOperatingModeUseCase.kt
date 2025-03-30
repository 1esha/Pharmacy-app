package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import kotlinx.coroutines.flow.Flow

/**
 * Класс [GetOperatingModeUseCase] является UseCase для получения списка режимов работы аптек.
 *
 * Параметры:
 * [catalogRepository] - репозиторий с функционалом.
 */
class GetOperatingModeUseCase(
    private val catalogRepository: CatalogRepository
) {

    fun execute(): Flow<Result> {
        val result = catalogRepository.getOperatingModeFlow()

        return result
    }

}