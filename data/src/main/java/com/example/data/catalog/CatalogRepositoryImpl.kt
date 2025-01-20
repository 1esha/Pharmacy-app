package com.example.data.catalog

import android.content.Context
import androidx.room.Room
import com.example.data.asSuccessResultDataSource
import com.example.data.catalog.datasource.local.CatalogRepositoryDataSourceLocalImpl
import com.example.data.catalog.datasource.local.FavoriteRoomDatabase
import com.example.data.catalog.datasource.remote.CatalogRepositoryDataSourceRemoteImpl
import com.example.data.toFavoriteEntity
import com.example.data.toResponseModel
import com.example.data.toResponseValueFavoriteModel
import com.example.data.toResponseValueListFavoriteModel
import com.example.data.toResponseValueListPharmacyAddressesModel
import com.example.data.toResponseValueListProductAvailabilityModel
import com.example.data.toResponseValueListProductModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.catalog.CatalogRepository
import com.example.domain.catalog.models.FavoriteModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.models.PharmacyAddressesModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel

class CatalogRepositoryImpl(context: Context) : CatalogRepository<
        ResponseValueModel<List<ProductModel>?>,
        ResponseValueModel<List<ProductAvailabilityModel>?>,
        ResponseValueModel<List<PharmacyAddressesModel>?>,
        ResponseValueModel<FavoriteModel>,
        ResponseValueModel<List<FavoriteModel>>,
        ResponseModel>{


    private val catalogRepositoryDataSourceRemoteImpl = CatalogRepositoryDataSourceRemoteImpl()

    private val favoriteRoomDatabase = Room.databaseBuilder(
        context = context,
        klass = FavoriteRoomDatabase::class.java,
        name = "favorite_database"
    ).build()

    private val catalogRepositoryDataSourceLocalImpl = CatalogRepositoryDataSourceLocalImpl(favoriteDao = favoriteRoomDatabase.favoriteDao())

    override suspend fun getAllProducts(): Result<ResponseValueModel<List<ProductModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getAllProducts()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductModel())

        return result
    }

    override suspend fun getProductsByPath(path: String): Result<ResponseValueModel<List<ProductModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getProductsByPath(path = path)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductModel())

        return result
    }

    override suspend fun getPharmacyAddresses(): Result<ResponseValueModel<List<PharmacyAddressesModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getPharmacyAddresses()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListPharmacyAddressesModel())

        return result
    }

    override suspend fun getProductAvailabilityByPath(path: String): Result<ResponseValueModel<List<ProductAvailabilityModel>?>> {
        val resultDataSource = catalogRepositoryDataSourceRemoteImpl.getProductAvailabilityByPath(path = path)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListProductAvailabilityModel())

        return result
    }

    override suspend fun getAllFavorites(): Result<ResponseValueModel<List<FavoriteModel>>> {
        val resultDataSource = catalogRepositoryDataSourceLocalImpl.getAllFavorites()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueListFavoriteModel())

        return result
    }

    override suspend fun getFavoriteById(productId: Int): Result<ResponseValueModel<FavoriteModel>> {
        val resultDataSource = catalogRepositoryDataSourceLocalImpl.getFavoriteById(productId = productId)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseValueFavoriteModel())

        return result
    }

    override suspend fun addFavorite(favoriteModel: FavoriteModel): Result<ResponseModel> {
        val favoriteEntity = favoriteModel.toFavoriteEntity()
        val resultDataSource = catalogRepositoryDataSourceLocalImpl.insertFavorite(favoriteEntity = favoriteEntity)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseModel())

        return result
    }

    override suspend fun deleteById(productId: Int): Result<ResponseModel> {
        val resultDataSource = catalogRepositoryDataSourceLocalImpl.deleteById(productId = productId)
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseModel())

        return result
    }

}