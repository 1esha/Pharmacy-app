package com.example.data.catalog.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.catalog.datasource.local.entity.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 1)
abstract class FavoriteRoomDatabase: RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

}