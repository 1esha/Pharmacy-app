package com.example.data.search.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.search.datasource.entity.SearchQueryEntity

@Database(entities = [SearchQueryEntity::class], version = 1)
abstract class SearchQueriesRoomDatabase: RoomDatabase() {

    abstract fun SearchDao():SearchDao

}