package com.example.data.search

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.search.datasource.SearchQueriesRoomDatabase
import com.example.data.search.datasource.SearchRepositoryDataSourceLocalImpl
import com.example.data.search.datasource.entity.SearchQueryEntity
import com.example.data.toResponseModel
import com.example.domain.Result
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.search.SearchRepository
import com.example.domain.search.models.SearchQueryModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class SearchRepositoryImpl(context: Context): SearchRepository {

    private val searchQueriesRoomDatabase = Room.databaseBuilder(
        context = context,
        klass = SearchQueriesRoomDatabase::class.java,
        name = "search_queries"
    ).build()

    private val searchRepositoryDataSourceLocalImpl = SearchRepositoryDataSourceLocalImpl(searchDao = searchQueriesRoomDatabase.SearchDao())

    override fun addSearchQuery(searchText: String): Flow<Result> = flow{
        try {
            searchRepositoryDataSourceLocalImpl.insertSearchQuery(searchText = searchText).collect{ resultDataSource ->
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

    override fun deleteSearchQuery(searchQueryId: Int): Flow<Result> = flow{
        try {
            searchRepositoryDataSourceLocalImpl.deleteSearchQuery(searchQueryId = searchQueryId).collect{ resultDataSource ->
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

    override fun getAllSearchQueries(): Flow<Result> = flow{
        try {
            searchRepositoryDataSourceLocalImpl.getAllSearchQueries().collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response?.value != null){

                    val _listSearchQueryEntity = response.value as List<*>
                    val listSearchQueryEntity = _listSearchQueryEntity.map { it as SearchQueryEntity }

                    val listSearchQueryModel = listSearchQueryEntity.toListSearchQueryModel()

                    val data = ResponseValueModel(
                        value = listSearchQueryModel,
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

    override fun deleteAllSearchQueries(): Flow<Result> = flow{
        try {
            searchRepositoryDataSourceLocalImpl.deleteAllSearchQueries().collect{ resultDataSource ->
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

    private fun List<SearchQueryEntity>.toListSearchQueryModel(): List<SearchQueryModel>{
        val mutableListSearchQueryModel = mutableListOf<SearchQueryModel>()

        this.forEach { searchQueryEntity ->
            mutableListSearchQueryModel.add(
                searchQueryEntity.toSearchQueryModel()
            )
        }

        return mutableListSearchQueryModel
    }

    private fun SearchQueryEntity.toSearchQueryModel(): SearchQueryModel {
        return SearchQueryModel(
            searchQueryId = this.searchQueryId,
            searchText = this.searchText
        )
    }

}