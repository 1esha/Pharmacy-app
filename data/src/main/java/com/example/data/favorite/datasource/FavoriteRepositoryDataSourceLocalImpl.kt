package com.example.data.favorite.datasource


import com.example.data.ErrorResultDataSource
import com.example.data.ResultDataSource
import com.example.data.SUCCESS
import com.example.data.SUCCESS_CODE
import com.example.data.SuccessResultDataSource
import com.example.data.favorite.datasource.entity.FavoriteEntity
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoriteRepositoryDataSourceLocalImpl(private val favoriteDao: FavoriteDao):
    FavoriteRepositoryDataSourceLocal<
            ResponseValueDataSourceModel<FavoriteEntity>,
            ResponseValueDataSourceModel<List<FavoriteEntity>>,
            ResponseDataSourceModel> {


    override suspend fun getAllFavorites(): ResultDataSource<ResponseValueDataSourceModel<List<FavoriteEntity>>> = withContext(Dispatchers.IO) {
        try {
            val listFavoriteEntity = favoriteDao.getAllFavorites()
            val responseValueDataSourceModel = ResponseValueDataSourceModel(
                value = listFavoriteEntity,
                responseDataSourceModel = ResponseDataSourceModel(
                    message = SUCCESS,
                    status = SUCCESS_CODE
                )
            )
            return@withContext SuccessResultDataSource(value = responseValueDataSourceModel)
        }
        catch (e: Exception) {
            return@withContext ErrorResultDataSource(exception = e)
        }
    }

    override suspend fun getFavoriteById(productId: Int): ResultDataSource<ResponseValueDataSourceModel<FavoriteEntity>> = withContext(Dispatchers.IO) {
        try {
            val favoriteEntity = favoriteDao.getFavoriteById(productId = productId)
            val responseValueDataSourceModel = ResponseValueDataSourceModel(
                value = favoriteEntity,
                responseDataSourceModel = ResponseDataSourceModel(
                    message = SUCCESS,
                    status = SUCCESS_CODE
                )
            )
            return@withContext SuccessResultDataSource(value = responseValueDataSourceModel)
        }
        catch (e: Exception) {
            return@withContext ErrorResultDataSource(exception = e)
        }
    }

    override suspend fun insertFavorite(favoriteEntity: FavoriteEntity): ResultDataSource<ResponseDataSourceModel> = withContext(Dispatchers.IO){
        try {
            favoriteDao.insertFavorite(favoriteEntity = favoriteEntity)
            return@withContext SuccessResultDataSource(value = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )
            )
        }
        catch (e: Exception) {
            return@withContext ErrorResultDataSource(exception = e)
        }
    }

    override suspend fun deleteById(productId: Int): ResultDataSource<ResponseDataSourceModel>  = withContext(Dispatchers.IO){
        try {
            favoriteDao.deleteById(productId = productId)
            return@withContext SuccessResultDataSource(value = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )
            )
        }
        catch (e: Exception) {
            return@withContext ErrorResultDataSource(exception = e)
        }
    }

    override suspend fun deleteAllFavorite(): ResultDataSource<ResponseDataSourceModel> = withContext(Dispatchers.IO){
        try {
            favoriteDao.deleteAllFavorite()
            return@withContext SuccessResultDataSource(value = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )
            )
        }
        catch (e: Exception) {
            return@withContext ErrorResultDataSource(exception = e)
        }
    }

}