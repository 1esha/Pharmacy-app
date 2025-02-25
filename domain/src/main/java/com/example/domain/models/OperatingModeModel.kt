package com.example.domain.models

import java.time.LocalTime

data class OperatingModeModel(
    val modeId: Int,
    val dayWeek: Int,
    val timeFrom: LocalTime,
    val timeBefore: LocalTime
)
