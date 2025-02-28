package com.example.data.catalog.datasource.models

data class OperatingModeDataSourceModel(
    val modeId: Int,
    val dayWeek: Int,
    val timeFrom: String,
    val timeBefore: String
)
