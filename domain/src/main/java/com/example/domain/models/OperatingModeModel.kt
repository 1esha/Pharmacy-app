package com.example.domain.models

data class OperatingModeModel(
    val modeId: Int,
    val dayWeek: Int,
    val timeFrom: String,
    val timeBefore: String
)
