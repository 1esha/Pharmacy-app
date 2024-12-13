package com.example.pharmacyapp

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController

const val KEY_USER_ID = "KEY_USER_ID"
const val UNAUTHORIZED_USER = -1
const val KEY_IS_INIT = "KEY_IS_INIT"
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



