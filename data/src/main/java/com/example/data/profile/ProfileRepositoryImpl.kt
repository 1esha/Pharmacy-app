package com.example.data.profile

import android.util.Log
import com.example.data.HttpClient
import com.example.data.asError
import com.example.data.asSuccess
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.remote.ProfileRepositoryDataSourceRemoteImpl
import com.example.data.toLogInDataSourceModel
import com.example.data.toResponseModel
import com.example.data.toUserDataSourceModel
import com.example.data.toUserInfoDataSourceModel
import com.example.data.toUserModel
import com.example.domain.Result
import com.example.domain.profile.ProfileRepository
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 * Класс [ProfileRepositoryImpl] является реализацией интерфейса [ProfileRepository].
 */
class ProfileRepositoryImpl : ProfileRepository {

    private val client = HttpClient().client

    private val profileRepositoryDataSourceRemote = ProfileRepositoryDataSourceRemoteImpl(client = client)

    /**
     * Создание нового пользователя.
     * При успешном результате эмитится ответ с сервера ( объект типа [ResponseModel] ).
     *
     * Параметры:
     * [userInfoModel] - информация пользователя.
     */
    override fun createUserFlow(userInfoModel: UserInfoModel): Flow<Result> = flow{
        try {
            val userInfoDataSourceModel = userInfoModel.toUserInfoDataSourceModel()
            profileRepositoryDataSourceRemote.createUserFlow(
                userInfoDataSourceModel = userInfoDataSourceModel
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение данных пользователя по логину и паролю.
     * При успешном результате эмитится модель данных пользователя ( [UserModel] ).
     *
     * Параметры:
     * [logInModel] - модель данных с логином и паролем.
     */
    override fun getUserFlow(logInModel: LogInModel): Flow<Result> = flow{
        try {
            val logInDataSourceModel = logInModel.toLogInDataSourceModel()
            profileRepositoryDataSourceRemote.getUserFlow(
                logInDataSourceModel = logInDataSourceModel
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val userDataSourceModel = response.value as UserDataSourceModel
                    val userModel = userDataSourceModel.toUserModel()

                    val data = ResponseValueModel(
                        value = userModel,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение идентификатора пользователя по информации пользователя.
     * При успешном результате эмитится идентификатор пользователя.
     *
     * Параметры:
     * [userInfoModel] - информация пользователя.
     */
    override fun getUserIdFlow(userInfoModel: UserInfoModel): Flow<Result> = flow{
        try {
            val userInfoDataSourceModel = userInfoModel.toUserInfoDataSourceModel()
            profileRepositoryDataSourceRemote.getUserIdFlow(
                userInfoDataSourceModel = userInfoDataSourceModel
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val userId = response.value as Int

                    val data = ResponseValueModel(
                        value = userId,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение данных пользователя по его идентификатору.
     * При успешном результате эмитится модель данных пользователя ( [UserModel] ).
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getUserByIdFlow(userId: Int): Flow<Result> = flow{
        try {
            profileRepositoryDataSourceRemote.getUserByIdFlow(userId = userId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val userDataSourceModel = response.value as UserDataSourceModel
                    val userModel = userDataSourceModel.toUserModel()

                    val data = ResponseValueModel(
                        value = userModel,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Редактирование данных пользователя.
     * При успешном результате эмитится ответ с сервера ( объект типа [ResponseModel] ).
     *
     * Параметры:
     * [userModel] - новые данные пользователя.
     */
    override fun editUserFlow(userModel: UserModel): Flow<Result> = flow{
        try {
            val userDataSourceModel = userModel.toUserDataSourceModel()
            profileRepositoryDataSourceRemote.editUserFlow(
                userDataSourceModel = userDataSourceModel
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Удаление аккаунта пользователя.
     * При успешном результате эмитится ответ с сервера ( объект типа [ResponseModel] ).
     *
     * Параметры:
     * [userId] - идентификатор пользователя аккаунт которого будет удален.
     */
    override fun deleteUserFlow(userId: Int): Flow<Result> = flow{
        try {
            profileRepositoryDataSourceRemote.deleteUserFlow(userId = userId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Получение города, который выбрал пользователь.
     * При успешном результате эмитится город пользователя.
     *
     * Параметры:
     * [userId] - идентификатор пользователя.
     */
    override fun getCityByUserIdFlow(userId: Int): Flow<Result> = flow{
        try {
            profileRepositoryDataSourceRemote.getCityByUserIdFlow(userId = userId).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseValueDataSourceModel<*>?

                if (response != null) {
                    val city = response.value as String

                    val data = ResponseValueModel(
                        value = city,
                        responseModel = response.responseDataSourceModel.toResponseModel()
                    )

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

    override fun changeUserPasswordFlow(
        userId: Int,
        oldUserPassword: Int,
        newUserPassword: Int
    ): Flow<Result> = flow{
        try {
            profileRepositoryDataSourceRemote.changeUserPasswordFlow(
                userId = userId,
                oldUserPassword = oldUserPassword,
                newUserPassword = newUserPassword
            ).collect{ resultDataSource ->
                val response = resultDataSource.asSuccess()?.data as ResponseDataSourceModel?

                if (response != null) {
                    val data = response.toResponseModel()

                    emit(Result.Success(data = data))
                }
                else {
                    val resultError = resultDataSource.asError()
                    if (resultError != null) {
                        emit(Result.Error(exception = resultError.exception))
                    }
                    else throw IllegalArgumentException("Несуществующий тип результата")
                }

            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }.flowOn(Dispatchers.IO)

}