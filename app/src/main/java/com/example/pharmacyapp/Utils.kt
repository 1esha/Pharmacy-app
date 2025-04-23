package com.example.pharmacyapp

import android.content.Context
import android.os.Bundle
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.domain.DisconnectionException
import com.example.domain.IdentificationException
import com.example.domain.InputDataException
import com.google.android.material.appbar.MaterialToolbar
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlin.Exception
import kotlin.math.roundToInt

const val UNAUTHORIZED_USER = -1
const val CLUB_DISCOUNT = 3.0
const val EMPTY_STRING = ""

/**
 * Минимальная задержка для запроса.
 * Нужна чтобы пользователь успел увидеть progress bar, а не моргание экрана.
 */
const val MIN_DELAY = 180L

/**
 * Максимальное количество товара, которое может забранировать пользователь.
 */
const val MAX_NUMBER_PRODUCT_IN_BASKET = 10

const val KEY_USER_ID = "KEY_USER_ID"
const val KEY_USER_NUMBER_PHONE = "KEY_USER_NUMBER_PHONE"
const val KEY_PRODUCT_ID = "KEY_PRODUCT_ID"
const val KEY_IS_INIT = "KEY_IS_INIT"
const val KEY_IS_FAVORITES = "KEY_IS_FAVORITES"
const val KEY_IS_IN_BASKET = "KEY_IS_IN_BASKET"
const val KEY_PATH_MAIN = "KEY_PATH_MAIN"
const val KEY_PATH = "KEY_PATH"
const val KEY_SEARCH_TEXT = "KEY_SEARCH_TEXT"
const val KEY_IS_CHECKED_DISCOUNT = "KEY_IS_CHECKED_DISCOUNT"
const val KEY_PRICE_FROM = "KEY_PRICE_FROM"
const val KEY_DEFAULT_PRICE_FROM = "KEY_DEFAULT_PRICE_FROM"
const val KEY_PRICE_UP_TO = "KEY_PRICE_UP_TO"
const val KEY_DEFAULT_PRICE_UP_TO = "KEY_DEFAULT_PRICE_UP_TO"
const val KEY_FIRST_NAME = "KEY_FIRST_NAME"
const val KEY_LAST_NAME = "KEY_LAST_NAME"
const val KEY_CITY = "KEY_CITY"
const val KEY_ARRAY_LIST_CURRENT_ITEMS = "KEY_ARRAY_LIST_CURRENT_ITEMS"
const val KEY_ARRAY_LIST_SELECTED_ADDRESSES = "KEY_ARRAY_LIST_SELECTED_ADDRESSES"
const val KEY_ARRAY_LIST_IDS_FILTERED = "KEY_ARRAY_LIST_IDS_FILTERED"
const val KEY_ARRAY_LIST_TITLES_INSTRUCTION = "KEY_ARRAY_LIST_TITLES_INSTRUCTION"
const val KEY_ARRAY_LIST_BODY_INSTRUCTION = "KEY_ARRAY_LIST_BODY_INSTRUCTION"
const val KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP = "KEY_ARRAY_LIST_IDS_PRODUCTS_FOR_MAP"
const val KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP = "KEY_ARRAY_LIST_NUMBER_PRODUCTS_FOR_MAP"
const val KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES = "KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES"
const val KEY_RESULT_ARRAY_LIST_IDS_FILTERED = "KEY_RESULT_ARRAY_LIST_IDS_FILTERED"
const val KEY_RESULT_FROM_EDIT_USER = "KEY_RESULT_FROM_EDIT_USER"
const val KEY_RESULT_FROM_PRODUCT_INFO = "KEY_RESULT_FROM_PRODUCT_INFO"
const val KEY_FLAGS_FOR_MAP = "KEY_FLAGS_FOR_MAP"
const val KEY_FLAGS_FOR_PRODUCTS = "KEY_FLAGS_FOR_PRODUCTS"

const val TYPE_EMPTY_RESULT = "TYPE_EMPTY_RESULT"
const val TYPE_GET_USER = "TYPE_GET_USER"
const val TYPE_GET_USER_ID = "TYPE_GET_USER_ID"
const val TYPE_GET_USER_BY_ID = "TYPE_GET_USER_BY_ID"
const val TYPE_EDIT_USER = "TYPE_EDIT_USER"
const val TYPE_DELETE_USER = "TYPE_DELETE_USER"
const val TYPE_GET_PRODUCTS_BY_PATH = "TYPE_GET_PRODUCTS_BY_PATH"
const val TYPE_GET_PRODUCT_BY_ID = "TYPE_GET_PRODUCT_BY_ID"
const val TYPE_GET_PHARMACY_ADDRESSES = "TYPE_GET_PHARMACY_ADDRESSES"
const val TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH = "TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH"
const val TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID = "TYPE_GET_PRODUCT_AVAILABILITY_BY_PRODUCT_ID"
const val TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID = "TYPE_GET_PRODUCT_AVAILABILITY_BY_ADDRESS_ID"
const val TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS = "TYPE_GET_PRODUCT_AVAILABILITY_BY_IDS_PRODUCTS"
const val TYPE_ADD_FAVORITE = "TYPE_ADD_FAVORITE"
const val TYPE_GET_ALL_FAVORITES = "TYPE_GET_ALL_FAVORITES"
const val TYPE_REMOVE_FAVORITES = "TYPE_REMOVE_FAVORITES"
const val TYPE_DELETE_ALL_FAVORITES = "TYPE_DELETE_ALL_FAVORITES"
const val TYPE_GET_CITY_BY_USER_ID = "KEY_GET_CITY_BY_USER_ID"
const val TYPE_GET_PHARMACY_ADDRESSES_DETAILS = "TYPE_GET_PHARMACY_ADDRESSES_DETAILS"
const val TYPE_GET_OPERATING_MODE = "TYPE_GET_OPERATING_MODE"
const val TYPE_GET_IDS_PRODUCTS_FROM_BASKET = "TYPE_GET_IDS_PRODUCTS_FROM_BASKET"
const val TYPE_ADD_PRODUCT_IN_BASKET = "TYPE_ADD_PRODUCT_IN_BASKET"
const val TYPE_DELETE_PRODUCT_FROM_BASKET = "TYPE_DELETE_PRODUCT_FROM_BASKET"
const val TYPE_DELETE_PRODUCTS_FROM_BASKET = "TYPE_DELETE_PRODUCTS_FROM_BASKET"
const val TYPE_GET_PRODUCTS_FROM_BASKET = "TYPE_GET_PRODUCTS_FROM_BASKET"
const val TYPE_GET_PRODUCTS_BY_IDS = "TYPE_GET_PRODUCTS_BY_IDS"
const val TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET = "TYPE_UPDATE_NUMBER_PRODUCTS_IN_BASKET"
const val TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET = "TYPE_UPDATE_NUMBERS_PRODUCTS_IN_BASKET"
const val TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY = "TYPE_UPDATE_NUMBERS_PRODUCTS_IN_PHARMACY"
const val TYPE_CREATE_ORDER = "TYPE_CREATE_ORDER"
const val TYPE_GET_PURCHASE_HISTORY= "TYPE_GET_PURCHASE_HISTORY"
const val TYPE_GET_CURRENT_ORDERS = "TYPE_GET_CURRENT_ORDERS"
const val TYPE_GET_PRODUCTS_BY_SEARCH = "TYPE_GET_PRODUCTS_BY_SEARCH"
const val TYPE_SAVE_TEXT_SEARCH = "TYPE_SAVE_TEXT_SEARCH"
const val TYPE_GET_ALL_SEARCH_QUERIES = "TYPE_GET_ALL_SEARCH_QUERIES"
const val TYPE_DELETE_SEARCH_QUERY = "TYPE_DELETE_SEARCH_QUERY"
const val TYPE_CLEAR_ALL_SEARCH_HISTORY = "TYPE_CLEAR_ALL_SEARCH_HISTORY"

const val FLAG_PENDING_RESULT = "FLAG_PENDING_RESULT"
const val FLAG_ERROR_RESULT = "FLAG_ERROR_RESULT"
const val FLAG_SUCCESS_RESULT = "FLAG_SUCCESS_RESULT"

// флаг показа разметки для экрана всех аптек
const val FLAG_ALL_PHARMACIES = "FLAG_ALL_PHARMACIES"

// флаг для показа разметки для экрана текущего товара
const val FLAG_CURRENT_PRODUCT = "FLAG_CURRENT_PRODUCT"

// флаг для показа разметки для экрана выбора аптеки для оформления заказа
const val FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING = "FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING"

const val FLAG_PRODUCTS_BY_PATH = "FLAG_PRODUCTS_BY_PATH"
const val FLAG_PRODUCTS_BY_SEARCH = "FLAG_PRODUCTS_BY_SEARCH"

const val NAME_SHARED_PREFERENCES = "NAME_SHARED_PREFERENCES"

typealias OnClickNavigationIcon = () -> Unit

interface SupportActivity {

    fun getNavControllerMain(): NavController

    fun isNetworkStatus(context: Context): Boolean

    fun showToast(message: String)

    fun getStringById(id: Int): String

    fun getVersionName(): String

    fun setFragmentResult(requestKey: String, result: Bundle)

    fun setFragmentResultListener(requestKey: String, callback: (String,Bundle) -> Unit)

}

fun Fragment.getSupportActivity(): SupportActivity {
    return requireActivity() as SupportActivity
}

fun String.toPath(): String {
    var path = ""
    this.toLowerCasePreservingASCIIRules().forEach { char ->
        path += when (char) {
            ' ' -> '_'
            '/' -> '-'
            else -> char
        }
    }

    return path
}

class ToolbarSettings(private val toolbar: MaterialToolbar) {

    fun installToolbarMain(icon: Int, title: String? = null, onClickNavigationIcon: () -> Unit) =
        with(toolbar) {
            setNavigationIcon(icon)
            setNavigationOnClickListener { onClickNavigationIcon() }
            if (title != null) setTitle(title)
        }

}

data class ToolbarSettingsModel(
    val title: String? = null,
    val icon: Int? = null,
    val subTitle: String? = null,
    val onClickNavigationIcon: OnClickNavigationIcon
)

fun getErrorMessage(exception: Exception): Int{
    return when(exception){
        is ConnectTimeoutException -> R.string.connection_error
        is DisconnectionException -> R.string.check_your_internet_connection
        is IdentificationException -> R.string.error_in_getting_the_id
        is InputDataException -> R.string.enter_the_data
        is NullPointerException -> R.string.invalid_value
        else -> R.string.unknown_error
    }
}

fun List<Int>.toArrayListInt(): ArrayList<Int> {
    val arrayList = arrayListOf<Int>()
    this.forEach {
        arrayList.add(it)
    }

    return arrayList
}

fun getPrice(discount: Double, price: Double): Int {

    val clubDiscount = CLUB_DISCOUNT

    val sumDiscount = ((discount) / 100) * price
    val priceDiscounted = price - sumDiscount
    val sumClubDiscount = ((clubDiscount) / 100) * priceDiscounted
    val priceClubDiscounted = priceDiscounted - sumClubDiscount

    return priceClubDiscounted.roundToInt()
}

/**
 * Класс [ColorUtils] помогает работать с идентификаторами цветов из кода.
 *
 * Параметры:
 * [context] - контекст создания экземпляра класса.
 */
class ColorUtils(private val context: Context){

    val colorPrimary = getColor(R.color.green800)
    val colorSecondaryContainer = getColor(R.color.green100)
    val colorOnSecondaryContainer = getColor(R.color.green900)
    val colorOnPrimary = getColor(R.color.white)

    /**
     * Получение цвета типа [Int] по его идентификатору.
     */
    fun getColor(@ColorRes color: Int): Int{
        return ContextCompat.getColor(context,color)
    }
}

fun String.toDateTime(): String{
    val year = this.substringBefore('-')
    val month = this.substringBeforeLast('-').substringAfter('-')
    val date = this.substringAfterLast('-').substringBefore(' ')
    val time = this.substringAfter(' ').substringBeforeLast(':')

    val dateTime = "$date.$month.$year $time"
    return dateTime
}


