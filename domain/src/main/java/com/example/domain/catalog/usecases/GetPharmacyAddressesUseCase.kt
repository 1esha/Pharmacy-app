package com.example.domain.catalog.usecases

import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.profile.models.ResponseValueModel

class GetPharmacyAddressesUseCase(
    private val catalogRepository: CatalogRepository<*,*,ResponseValueModel<List<PharmacyAddressesModel>?>,*>
) {

    suspend fun execute(): Result<ResponseValueModel<List<PharmacyAddressesModel>?>> {
        val result = catalogRepository.getPharmacyAddresses()

        return result
    }

}