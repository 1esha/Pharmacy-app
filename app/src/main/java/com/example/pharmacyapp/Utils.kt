package com.example.pharmacyapp

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.domain.DataEntryError
import com.example.domain.DisconnectionError
import com.example.domain.ErrorType
import com.example.domain.IdentificationError
import com.google.android.material.appbar.MaterialToolbar
import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlin.math.roundToInt

const val UNAUTHORIZED_USER = -1
const val EMPTY_STRING = ""

const val KEY_USER_ID = "KEY_USER_ID"
const val KEY_IS_INIT = "KEY_IS_INIT"
const val KEY_PATH_MAIN = "KEY_PATH_MAIN"
const val KEY_PATH = "KEY_PATH"
const val KEY_IS_CHECKED_DISCOUNT = "KEY_IS_CHECKED_DISCOUNT"
const val KEY_PRICE_FROM = "KEY_PRICE_FROM"
const val KEY_DEFAULT_PRICE_FROM = "KEY_DEFAULT_PRICE_FROM"
const val KEY_PRICE_UP_TO = "KEY_PRICE_UP_TO"
const val KEY_DEFAULT_PRICE_UP_TO = "KEY_DEFAULT_PRICE_UP_TO"
const val KEY_ARRAY_LIST_CURRENT_ITEMS = "KEY_ARRAY_LIST_CURRENT_ITEMS"
const val KEY_ARRAY_LIST_SELECTED_ADDRESSES = "KEY_ARRAY_LIST_SELECTED_ADDRESSES"
const val KEY_ARRAY_LIST_IDS_FILTERED = "KEY_ARRAY_LIST_IDS_FILTERED"
const val KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES = "KEY_RESULT_ARRAY_LIST_SELECTED_ADDRESSES"
const val KEY_RESULT_ARRAY_LIST_IDS_FILTERED = "KEY_RESULT_ARRAY_LIST_IDS_FILTERED"

const val TYPE_EMPTY = "TYPE_EMPTY"
const val TYPE_GET_USER_BY_ID = "TYPE_GET_USER_BY_ID"
const val TYPE_EDIT_USER = "TYPE_EDIT_USER"
const val TYPE_DELETE_USER = "TYPE_DELETE_USER"
const val TYPE_OTHER = "TYPE_OTHER"
const val TYPE_GET_PRODUCTS_BY_PATH = "TYPE_GET_PRODUCTS_BY_PATH"
const val TYPE_GET_PHARMACY_ADDRESSES = "TYPE_GET_PHARMACY_ADDRESSES"
const val TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH = "TYPE_GET_PRODUCT_AVAILABILITY_BY_PATH"

const val FLAG_PENDING_RESULT = "FLAG_PENDING_RESULT"
const val FLAG_ERROR_RESULT = "FLAG_ERROR_RESULT"
const val FLAG_SUCCESS_RESULT = "FLAG_SUCCESS_RESULT"

const val NAME_SHARED_PREFERENCES = "NAME_SHARED_PREFERENCES"

typealias OnClickNavigationIcon = () -> Unit

interface SupportActivity {

    fun getNavControllerMain(): NavController

    fun isNetworkStatus(context: Context): Boolean

    fun showToast(message: String)

    fun getStringById(id: Int): String

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
    val onClickNavigationIcon: OnClickNavigationIcon
)

data class MenuSettingsModel(
    val menu: Int,
    val onClickMenuItem: (Int) -> Unit
)

fun getMessageByErrorType(errorType: ErrorType?): Int {
    return when (errorType) {
        is DisconnectionError -> R.string.check_your_internet_connection
        is IdentificationError -> R.string.error_in_getting_the_id
        is DataEntryError -> R.string.enter_the_data
        else -> R.string.error
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

    val clubDiscount = 3.0

    val sumDiscount = ((discount) / 100) * price
    val priceDiscounted = price - sumDiscount
    val sumClubDiscount = ((clubDiscount) / 100) * priceDiscounted
    val priceClubDiscounted = priceDiscounted - sumClubDiscount

    return priceClubDiscounted.roundToInt()
}


