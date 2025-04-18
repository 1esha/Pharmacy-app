package com.example.data.search.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.search.datasource.entity.SearchQueryEntity

@Dao
interface SearchDao {

    @Insert
    fun insertSearchQuery(searchQueryEntity: SearchQueryEntity)

    @Query("DELETE FROM search_queries WHERE search_query_id = :searchQueryId")
    fun deleteSearchQuery(searchQueryId: Int)

    @Query("SELECT * FROM search_queries")
    fun getAllSearchQueries(): List<SearchQueryEntity>

    @Query("DELETE FROM search_queries")
    fun deleteAllSearchQueries()

}