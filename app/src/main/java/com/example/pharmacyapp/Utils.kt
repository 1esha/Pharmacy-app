package com.example.pharmacyapp

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.example.domain.DataEntryError
import com.example.domain.DisconnectionError
import com.example.domain.ErrorType
import com.example.domain.IdentificationError
import com.google.android.material.appbar.MaterialToolbar

const val KEY_USER_ID = "KEY_USER_ID"
const val UNAUTHORIZED_USER = -1
const val KEY_IS_INIT = "KEY_IS_INIT"
const val TYPE_GET_USER_BY_ID = "TYPE_GET_USER_BY_ID"
const val TYPE_EDIT_USER = "TYPE_EDIT_USER"
const val TYPE_DELETE_USER = "TYPE_DELETE_USER"
const val TYPE_OTHER = "TYPE_OTHER"
const val NAME_SHARED_PREFERENCES = "NAME_SHARED_PREFERENCES"

interface SupportActivity{

    fun getNavControllerMain(): NavController

    fun isNetworkStatus(context: Context): Boolean

    fun showToast(message: String)

    fun getStringById(id: Int): String

}

fun Fragment.getSupportActivity():SupportActivity{
    return requireActivity() as SupportActivity
}

class ToolbarSettings(private val toolbar: MaterialToolbar){

    fun installToolbarMain(icon: Int, onClickNavigationIcon:() -> Unit) = with(toolbar){
            setNavigationIcon(icon)
            setNavigationOnClickListener { onClickNavigationIcon() }

    }

}

fun getMessageByErrorType(errorType: ErrorType?): Int{
    return when(errorType){
        is DisconnectionError -> R.string.check_your_internet_connection
        is IdentificationError -> R.string.error_in_getting_the_id
        is DataEntryError -> R.string.enter_the_data
        else -> R.string.error
    }
}



