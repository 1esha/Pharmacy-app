package com.example.data.favorite.datasource


import com.example.data.ResultDataSource
import com.example.data.SUCCESS
import com.example.data.SUCCESS_CODE
import com.example.data.favorite.datasource.entity.FavoriteEntity
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class FavoriteRepositoryDataSourceLocalImpl(private val favoriteDao: FavoriteDao):
    FavoriteRepositoryDataSourceLocal {

    /**
     * Получение списка со всеми избранными товарами.
     */
    override fun getAllFavoritesFlow(): Flow<ResultDataSource> = flow{
        try {
            val listFavoriteEntity = favoriteDao.getAllFavorites()
            val data = ResponseValueDataSourceModel(
                value = listFavoriteEntity,
                responseDataSourceModel = ResponseDataSourceModel(
                    message = SUCCESS,
                    status = SUCCESS_CODE
                )
            )

            emit(ResultDataSource.Success(data = data))
        }
        catch (e: Exception) {
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение товара из списка избранного по его идентификатору.
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет получен.
     */
    override fun getFavoriteByIdFlow(productId: Int): Flow<ResultDataSource> = flow{
        try {
            val favoriteEntity = favoriteDao.getFavoriteById(productId = productId)
            val data = ResponseValueDataSourceModel(
                value = favoriteEntity,
                responseDataSourceModel = ResponseDataSourceModel(
                    message = SUCCESS,
                    status = SUCCESS_CODE
                )
            )

            emit(ResultDataSource.Success(data = data))
        }
        catch (e: Exception) {
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Добавление товара в список избранного.
     *
     * Параметры:
     * [favoriteEntity] - данные о товаре, которые будут добавлены.
     */
    override fun insertFavoriteFlow(favoriteEntity: FavoriteEntity): Flow<ResultDataSource> = flow{
        try {
            favoriteDao.insertFavorite(favoriteEntity = favoriteEntity)
            val data = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )

            emit(ResultDataSource.Success(data = data))
        }
        catch (e: Exception) {
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление товара из списка избранного по его идентификатору.
     *
     * Параметры:
     * [productId] - идентификатор удаляемого товара.
     */
    override fun deleteByIdFlow(productId: Int): Flow<ResultDataSource> = flow{
        try {
            favoriteDao.deleteById(productId = productId)
            val data = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )

            emit(ResultDataSource.Success(data = data))
        }
        catch (e: Exception) {
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление всех товаров из списка избранного.
     */
    override fun deleteAllFavoriteFlow(): Flow<ResultDataSource> = flow{
        try {
            favoriteDao.deleteAllFavorite()
            val data = ResponseDataSourceModel(
                message = SUCCESS,
                status = SUCCESS_CODE
            )

            emit(ResultDataSource.Success(data = data))
        }
        catch (e: Exception) {
            emit(ResultDataSource.Error(exception = e))
        }
    }.flowOn(Dispatchers.IO)

}