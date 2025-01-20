package com.example.data.catalog.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.data.catalog.datasource.local.entity.FavoriteEntity

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites")
    fun getAllFavorites():List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE product_id = :productId")
    fun getFavoriteById(productId: Int): FavoriteEntity

    @Insert
    fun insertFavorite(favoriteEntity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE product_id = :productId")
    fun deleteById(productId: Int)
}