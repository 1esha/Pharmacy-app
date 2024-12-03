package com.example.domain

import com.example.domain.profile.models.ResponseModel

sealed class Result <T>

class PendingResult<T>: Result<T>()

class SuccessResult<T>(
    val value: T?
): Result<T>()

class ErrorResult<T>(
    val exception: Exception
) : Result<T>()