package com.example.data

import android.util.Log
import com.example.data.basket.datasource.models.NumberProductsDataSourceModel
import com.example.data.catalog.datasource.models.OperatingModeDataSourceModel
import com.example.data.favorite.datasource.entity.FavoriteEntity
import com.example.data.catalog.datasource.models.PharmacyAddressesDataSourceModel
import com.example.data.catalog.datasource.models.PharmacyAddressesDetailsDataSourceModel
import com.example.data.catalog.datasource.models.ProductAvailabilityDataSourceModel
import com.example.data.catalog.datasource.models.ProductDataSourceModel
import com.example.data.profile.datasource.models.LogInDataSourceModel
import com.example.data.profile.datasource.models.ResponseDataSourceModel
import com.example.data.profile.datasource.models.UserDataSourceModel
import com.example.data.profile.datasource.models.UserInfoDataSourceModel
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.domain.favorite.models.FavoriteModel
import com.example.domain.catalog.models.ProductAvailabilityModel
import com.example.domain.catalog.models.ProductModel
import com.example.domain.catalog.models.PharmacyAddressesModel
import com.example.domain.models.NumberProductsModel
import com.example.domain.models.OperatingModeModel
import com.example.domain.profile.models.LogInModel
import com.example.domain.profile.models.ResponseModel
import com.example.domain.profile.models.UserInfoModel
import com.example.domain.profile.models.UserModel

/**
 *  Utils.kt содержит функции
 *  для преобразования типов из DataSource в типы для репозитория
 */
const val SUCCESS = "Успешно"
const val SUCCESS_CODE = 200
const val NOT_SELECTED = "NOT_SELECTED"


fun ResponseDataSourceModel.toResponseModel():ResponseModel{
    return ResponseModel(
        message = this.message,
        status = this.status
    )
}

fun List<NumberProductsModel>.toListNumberProductsDataSourceModel(): List<NumberProductsDataSourceModel> {
    val mutableListNumberProductsDataSourceModel = mutableListOf<NumberProductsDataSourceModel>()

    this.forEach {
        mutableListNumberProductsDataSourceModel.add(
            it.toNumberProductsDataSourceModel()
        )
    }

    return mutableListNumberProductsDataSourceModel
}

private fun NumberProductsModel.toNumberProductsDataSourceModel(): NumberProductsDataSourceModel {
    return NumberProductsDataSourceModel(
        productId = this.productId,
        numberProducts = this.numberProducts
    )
}


// ProfileRepository

fun UserInfoModel.toUserInfoDataSourceModel(isGeneratedHashCode: Boolean = false): UserInfoDataSourceModel {
    val userPasswordHashCode = if (isGeneratedHashCode) this.userPassword.hashCode() else {
        try {
            userPassword.toInt()
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    return UserInfoDataSourceModel(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        userPasswordHashCode = userPasswordHashCode,
        city = this.city
    )

}

fun UserInfoDataSourceModel.toUserInfoModel(): UserInfoModel {
    return UserInfoModel(
        firstName = this.firstName,
        lastName = this.lastName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        userPassword = this.userPasswordHashCode.toString(),
        city = this.city
    )
}

fun UserDataSourceModel.toUserModel(): UserModel{
    return UserModel(
        userId = this.userId,
        userInfoModel = this.userInfoModel.toUserInfoModel()
    )
}

fun LogInModel.toLogInDataSourceModel(): LogInDataSourceModel {
    return LogInDataSourceModel(
        login = this.login,
        userPasswordHashCode = this.userPassword.hashCode()
    )
}

fun UserModel.toUserDataSourceModel(): UserDataSourceModel{
    return UserDataSourceModel(
        userId = this.userId,
        userInfoModel = this.userInfoModel.toUserInfoDataSourceModel(isGeneratedHashCode = false)
    )
}

// CatalogRepository

fun ProductDataSourceModel.toProductModel(): ProductModel {
    val productDataSourceModel = this
    return ProductModel(
        productId = productDataSourceModel.productId,
        title = productDataSourceModel.title,
        productPath = productDataSourceModel.productPath,
        price = productDataSourceModel.price,
        discount = productDataSourceModel.discount,
        productBasicInfo = productDataSourceModel.productBasicInfo,
        productDetailedInfo = productDataSourceModel.productDetailedInfo,
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

// FavoriteRepository

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
