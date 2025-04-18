package com.example.data.search.datasource.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_queries")
data class SearchQueryEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "search_query_id")
    val searchQueryId: Int = 0,
    @ColumnInfo(name = "search_text")
    val searchText: String
)
