package com.example.data.search.datasource

import com.example.data.ResultDataSource
import com.example.data.SUCCESS
import com.example.data.SUCCESS_CODE
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.search.datasource.entity.SearchQueryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepositoryDataSourceLocalImpl(
    private val searchDao: SearchDao
): SearchRepositoryDataSourceLocal {

    override fun insertSearchQuery(searchText: String): Flow<ResultDataSource> = flow{
        try {
            searchDao.insertSearchQuery(
                searchQueryEntity = SearchQueryEntity(
                    searchText = searchText
                )
            )
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

    override fun deleteSearchQuery(searchQueryId: Int): Flow<ResultDataSource> = flow{
        try {
            searchDao.deleteSearchQuery(searchQueryId = searchQueryId)
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

    override fun getAllSearchQueries(): Flow<ResultDataSource> = flow{
        try {
            val listSearchQueryEntity = searchDao.getAllSearchQueries()
            val data = ResponseValueDataSourceModel(
                value = listSearchQueryEntity,
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

    override fun deleteAllSearchQueries(): Flow<ResultDataSource> = flow{
        try {
            searchDao.deleteAllSearchQueries()
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