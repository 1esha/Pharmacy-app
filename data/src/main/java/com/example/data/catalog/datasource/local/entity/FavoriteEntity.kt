package com.example.data.catalog.datasource.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey()
    @NotNull
    @ColumnInfo(name = "product_id")
    val productId: Int,
    val title: String,
    @ColumnInfo(name = "product_path")
    val productPath: String,
    val price: Double,
    val discount: Double,
    val image: String
)
