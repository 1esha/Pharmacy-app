package com.example.data.catalog.datasource.models

import java.time.LocalTime

data class OperatingModeDataSourceModel(
    val modeId: Int,
    val dayWeek: Int,
    val timeFrom: LocalTime,
    val timeBefore: LocalTime
)
