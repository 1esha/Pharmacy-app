package com.example.data.favorite

import android.content.Context
import androidx.room.Room
import com.example.data.asSuccessResultDataSource
import com.example.data.favorite.datasource.FavoriteRepositoryDataSourceLocalImpl
import com.example.data.favorite.datasource.FavoriteRoomDatabase
import com.example.data.toFavoriteEntity
import com.example.data.toResponseModel
import com.example.data.toResponseValueFavoriteModel
import com.example.data.toResponseValueListFavoriteModel
import com.example.data.toResult
import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel

class FavoriteRepositoryImpl(context: Context): FavoriteRepository<
        ResponseValueModel<FavoriteModel>,
        ResponseValueModel<List<FavoriteModel>>,
        ResponseModel> {

    private val favoriteRoomDatabase = Room.databaseBuilder(
        context = context,
        klass = FavoriteRoomDatabase::class.java,
        name = "favorite_database"
    ).build()

    private val catalogRepositoryDataSourceLocalImpl = FavoriteRepositoryDataSourceLocalImpl(favoriteDao = favoriteRoomDatabase.favoriteDao())

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

    override suspend fun insertFavorite(favoriteModel: FavoriteModel): Result<ResponseModel> {
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

    override suspend fun deleteAllFavorite(): Result<ResponseModel> {
        val resultDataSource = catalogRepositoryDataSourceLocalImpl.deleteAllFavorite()
        val value = resultDataSource.asSuccessResultDataSource()?.value
        val result = resultDataSource.toResult(value = value?.toResponseModel())

        return result
    }
}