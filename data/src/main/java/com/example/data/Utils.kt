package com.example.data

import com.example.data.catalog.datasource.models.OperatingModeDataSourceModel
import com.example.data.favorite.datasource.entity.FavoriteEntity
import com.example.data.catalog.datasource.models.PharmacyAddressesDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDetailsDataSourceModel
import com.example.data.catalog.datasource.models.ProductAvailabilityDataSourceModel
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.ResponseValueDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
import com.example.domain.ErrorResult
import com.example.domain.PendingResult
import com.example.domain.Result
import com.example.domain.SuccessResult
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.ResponseValueModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

/**
 *  Utils.kt содержит функции
 *  для преобразования типов из DataSource в типы для репозитория
 */

const val SUCCESS = "Успешно"
const val SUCCESS_CODE = 200
const val NOT_SELECTED = "NOT_SELECTED"


fun <I,O> ResultDataSource<I>.toResult(value:O?):Result<O>{
    return when(this){
        is PendingResultDataSource<I> -> {
            PendingResult<O>()
        }
        is SuccessResultDataSource<I> -> {
            SuccessResult<O>(
                value = value
            )
        }
        is ErrorResultDataSource<I> -> {
            ErrorResult<O>(
                exception = this.exception
            )
        }
    }
}

fun ResponseDataSourceModel.toResponseModel():ResponseModel{
    return ResponseModel(
        message = this.message,
        status = this.status
    )
}

fun <T> ResultDataSource<T>.asSuccessResultDataSource(): SuccessResultDataSource<T>?{
    return if (this is SuccessResultDataSource<T>) this else null
}

// ProfileRepository

fun UserInfoModel.toUserInfoDataSourceModel(): UserInfoDataSourceModel {

    return UserInfoDataSourceModel(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        userPassword = this.userPassword,
        city = this.city
    )
}


fun ResponseValueDataSourceModel<String>.toResponseValueStringModel(): ResponseValueModel<String> {
    return ResponseValueModel(
        value = this.value,
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<UserDataSourceModel>.toResponseValueUserModelModel(): ResponseValueModel<UserModel>{
    return ResponseValueModel(
        value = this.value?.let {
            UserModel(
                userId = it.userId,
                userInfoModel = UserInfoModel(
                    firstName = this.value.userInfoModel.firstName,
                    lastName = this.value.userInfoModel.lastName,
                    email = this.value.userInfoModel.email,
                    phoneNumber = this.value.userInfoModel.phoneNumber,
                    userPassword = this.value.userInfoModel.userPassword,
                    city = this.value.userInfoModel.city,
                )
            )
        },
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<Int>.toResponseValueIntModel(): ResponseValueModel<Int>{
    return ResponseValueModel(
        value = this.value ,
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun LogInModel.toLogInDataSourceModel(): LogInDataSourceModel {
    return LogInDataSourceModel(
        login = this.login,
        userPassword = this.userPassword
    )
}

fun UserModel.toUserDataSourceModel(): UserDataSourceModel{
    return UserDataSourceModel(
        userId = this.userId,
        userInfoModel = this.userInfoModel.toUserInfoDataSourceModel()
    )
}

// CatalogRepository

fun ProductDataSourceModel.toProductModel(): ProductModel {
    val productDataSourceModel = this
    return ProductModel(
        product_id = productDataSourceModel.product_id,
        title = productDataSourceModel.title,
        product_path = productDataSourceModel.product_path,
        price = productDataSourceModel.price,
        discount = productDataSourceModel.discount,
        product_basic_info = productDataSourceModel.product_basic_info,
        product_detailed_info = productDataSourceModel.product_detailed_info,
        image = productDataSourceModel.image
    )
}

fun List<ProductDataSourceModel>.toListProductModel(): List<ProductModel> {
    val listProductModel = mutableListOf<ProductModel>()
    this.forEach { productDataSourceModel ->
        listProductModel.add(productDataSourceModel.toProductModel())
    }

    return listProductModel
}

fun List<PharmacyAddressesDataSourceModel>.toListPharmacyAddressesModel(): List<PharmacyAddressesModel> {
    val listPharmacyAddressesModel = mutableListOf<PharmacyAddressesModel>()
    this.forEach { pharmacyAddressesDataSourceModel ->
        listPharmacyAddressesModel.add(
            PharmacyAddressesModel(
                addressId = pharmacyAddressesDataSourceModel.address_id,
                address = pharmacyAddressesDataSourceModel.address,
                city = pharmacyAddressesDataSourceModel.city
            )
        )
    }

    return listPharmacyAddressesModel
}

fun List<ProductAvailabilityDataSourceModel>.toListProductAvailabilityModel(): List<ProductAvailabilityModel> {
    val listProductAvailabilityModel = mutableListOf<ProductAvailabilityModel>()
    this.forEach { productAvailabilityDataSourceModel ->
        listProductAvailabilityModel.add(
            ProductAvailabilityModel(
                productId = productAvailabilityDataSourceModel.product_id,
                addressId = productAvailabilityDataSourceModel.address_id,
                productPath = productAvailabilityDataSourceModel.product_path,
                numberProducts = productAvailabilityDataSourceModel.number_products
            )
        )
    }

    return listProductAvailabilityModel
}

fun ResponseValueDataSourceModel<ProductDataSourceModel?>.toResponseValueProductModel(): ResponseValueModel<ProductModel?> {
    return ResponseValueModel(
        value = this.value?.toProductModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<List<ProductDataSourceModel>?>.toResponseValueListProductModel(): ResponseValueModel<List<ProductModel>?>{
    return ResponseValueModel(
        value = this.value?.toListProductModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<List<PharmacyAddressesDataSourceModel>?>.toResponseValueListPharmacyAddressesModel(): ResponseValueModel<List<PharmacyAddressesModel>?> {
    return ResponseValueModel(
        value = this.value?.toListPharmacyAddressesModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun ResponseValueDataSourceModel<List<ProductAvailabilityDataSourceModel>?>.toResponseValueListProductAvailabilityModel(): ResponseValueModel<List<ProductAvailabilityModel>?> {
    return ResponseValueModel(
        value = this.value?.toListProductAvailabilityModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun PharmacyAddressesDetailsDataSourceModel.toPharmacyAddressesDetailsModel(): PharmacyAddressesDetailsModel {
    return PharmacyAddressesDetailsModel(
        pharmacyAddressesModel = PharmacyAddressesModel(
            addressId = this.pharmacyAddressesDataSourceModel.address_id,
            address = this.pharmacyAddressesDataSourceModel.address,
            city = this.pharmacyAddressesDataSourceModel.city
        ),
        latitude = this.latitude,
        longitude = this.longitude,
        image = this.image,
        modeId = this.modeId
    )
}

fun List<PharmacyAddressesDetailsDataSourceModel>.toListPharmacyAddressesDetailsModel(): List<PharmacyAddressesDetailsModel> {

    val mutableListPharmacyAddressesDetailsModel = mutableListOf<PharmacyAddressesDetailsModel>()

    this.forEach { pharmacyAddressesDetailsDataSourceModel ->
        mutableListPharmacyAddressesDetailsModel.add(
            pharmacyAddressesDetailsDataSourceModel.toPharmacyAddressesDetailsModel()
        )
    }

    return mutableListPharmacyAddressesDetailsModel
}

fun ResponseValueDataSourceModel<List<PharmacyAddressesDetailsDataSourceModel>?>.toResponseValueListPharmacyAddressesDetailsDataSourceModel(): ResponseValueModel<List<PharmacyAddressesDetailsModel>?> {
    return ResponseValueModel(
        value = this.value?.toListPharmacyAddressesDetailsModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun OperatingModeDataSourceModel.toOperatingModeModel(): OperatingModeModel {
    return OperatingModeModel(
        modeId = this.modeId,
        dayWeek = this.dayWeek,
        timeFrom = this.timeFrom,
        timeBefore = this.timeBefore
    )
}

fun List<OperatingModeDataSourceModel>?.toListOperatingModeModel(): List<OperatingModeModel> {
    val mutableListOperatingMode = mutableListOf<OperatingModeModel>()

    this?.forEach { operatingModeDataSourceModel ->
        mutableListOperatingMode.add(operatingModeDataSourceModel.toOperatingModeModel())
    }

    return mutableListOperatingMode
}

fun ResponseValueDataSourceModel<List<OperatingModeDataSourceModel>?>.toResponseValueListOperatingModeModel(): ResponseValueModel<List<OperatingModeModel>?> {
    return ResponseValueModel(
        value = this.value.toListOperatingModeModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

// FavoriteRepository

fun ResponseValueDataSourceModel<List<FavoriteEntity>>.toResponseValueListFavoriteModel(): ResponseValueModel<List<FavoriteModel>> {
    return ResponseValueModel(
        value = this.value.toListFavoriteModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}

fun List<FavoriteEntity>?.toListFavoriteModel(): List<FavoriteModel> {
    val mutableListFavoriteModel = mutableListOf<FavoriteModel>()
    this?.forEach { favoriteEntity ->
        mutableListFavoriteModel.add(favoriteEntity.toFavoriteModel())
    }
    return mutableListFavoriteModel
}

fun FavoriteEntity.toFavoriteModel(): FavoriteModel {
    val favoriteEntity = this
    return FavoriteModel(
        productId = favoriteEntity.productId,
        title = favoriteEntity.title,
        productPath = favoriteEntity.productPath,
        price = favoriteEntity.price,
        discount = favoriteEntity.discount,
        image = favoriteEntity.image
    )
}

fun FavoriteModel.toFavoriteEntity() : FavoriteEntity {
    val favoriteModel = this
    return FavoriteEntity(
        productId = favoriteModel.productId,
        title = favoriteModel.title,
        productPath = favoriteModel.productPath,
        price = favoriteModel.price,
        discount = favoriteModel.discount,
        image = favoriteModel.image
    )
}

fun ResponseValueDataSourceModel<FavoriteEntity>.toResponseValueFavoriteModel(): ResponseValueModel<FavoriteModel> {
    return ResponseValueModel(
        value = this.value?.toFavoriteModel(),
        responseModel = this.responseDataSourceModel.toResponseModel()
    )
}