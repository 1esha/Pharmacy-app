package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.profile.models.ResponseValueModel

class GetPharmacyAddressesDetailsUseCase(
    private val catalogRepository: CatalogRepository<*, *, *, *, ResponseValueModel<List<PharmacyAddressesDetailsModel>?>,*>
) {

    suspend fun execute(): Result<ResponseValueModel<List<PharmacyAddressesDetailsModel>?>> {
        val result = catalogRepository.getPharmacyAddressesDetails()

        return result
    }

}