package com.example.data.favorite.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.favorite.datasource.entity.FavoriteEntity

@Database(entities = [FavoriteEntity::class], version = 1)
abstract class FavoriteRoomDatabase: RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

}