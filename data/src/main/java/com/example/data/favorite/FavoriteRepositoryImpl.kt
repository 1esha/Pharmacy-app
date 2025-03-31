package com.example.data.favorite

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.favorite.datasource.FavoriteRepositoryDataSourceLocalImpl
import com.example.data.favorite.datasource.FavoriteRoomDatabase
import com.example.data.favorite.datasource.entity.FavoriteEntity
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.toFavoriteEntity
import com.example.data.toFavoriteModel
import com.example.data.toListFavoriteModel
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.favorite.FavoriteRepository
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.profile.models.ResponseValueModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Класс [FavoriteRepositoryImpl] является репозиторием для работы с избранными товарами.
 */
class FavoriteRepositoryImpl(context: Context): FavoriteRepository {

    private val favoriteRoomDatabase = Room.databaseBuilder(
        context = context,
        klass = FavoriteRoomDatabase::class.java,
        name = "favorite_database"
    ).build()

    private val favoriteRepositoryDataSourceLocalImpl = FavoriteRepositoryDataSourceLocalImpl(favoriteDao = favoriteRoomDatabase.favoriteDao())

    /**
     * Получение списка со всеми избранными товарами.
     * При успешном результате эмитится список избранного ( List<[FavoriteModel]> ).
     */
    override fun getAllFavoritesFlow(): Flow<Result> = flow{
        try {
            favoriteRepositoryDataSourceLocalImpl.getAllFavoritesFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response?.value != null){
                    val _listAllFavoriteEntity = response.value as List<*>
                    val listAllFavoriteEntity = _listAllFavoriteEntity.map { it as  FavoriteEntity}

                    val listAllFavoriteModel = listAllFavoriteEntity.toListFavoriteModel()
                    val data = ResponseValueModel(
                        value = listAllFavoriteModel,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )
                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }

    }.flowOn(Dispatchers.IO)

    /**
     * Получение товара из списка избранного по его идентификатору.
     * При успешном результате эмитится товар типа [FavoriteModel].
     *
     * Параметры:
     * [productId] - идентификатор товара, который будет получен.
     */
    override fun getFavoriteByIdFlow(productId: Int): Flow<Result> = flow{
        try {
            favoriteRepositoryDataSourceLocalImpl.getFavoriteByIdFlow(productId = productId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response?.value != null){
                    val favoriteEntity = response.value as FavoriteEntity
                    val favoriteModel = favoriteEntity.toFavoriteModel()

                    val data = ResponseValueModel(
                        value = favoriteModel,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )
                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Добавление товара в список избранного.
     * При успешном результате эмитится успешный ответ типа [ResponseModel].
     *
     * Параметры:
     * [favoriteModel] - данные о товаре, которые будут добавлены.
     */
    override fun insertFavoriteFlow(favoriteModel: FavoriteModel): Flow<Result> = flow{
        try {
            val favoriteEntity = favoriteModel.toFavoriteEntity()
            favoriteRepositoryDataSourceLocalImpl.insertFavoriteFlow(favoriteEntity = favoriteEntity).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null){
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление товара из списка избранного по его идентификатору.
     * При успешном результате эмитится успешный ответ типа [ResponseModel].
     *
     * Параметры:
     * [productId] - идентификатор удаляемого товара.
     */
    override fun deleteByIdFlow(productId: Int): Flow<Result> = flow{
        try {
            favoriteRepositoryDataSourceLocalImpl.deleteByIdFlow(productId = productId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null){
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление всех товаров из списка избранного.
     * При успешном результате эмитится успешный ответ типа [ResponseModel].
     */
    override fun deleteAllFavoriteFlow(): Flow<Result> = flow{
        try {
            favoriteRepositoryDataSourceLocalImpl.deleteAllFavoriteFlow().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null){
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)
}