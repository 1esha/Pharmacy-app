package com.example.pharmacyapp.main.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.domain.catalog.models.PharmacyAddressesDetailsModel
import com.example.pharmacyapp.FLAG_ALL_PHARMACIES
import com.example.pharmacyapp.FLAG_CURRENT_PRODUCT
import com.example.pharmacyapp.FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING
import com.example.pharmacyapp.R
import java.util.ArrayList

class PharmacyDetailsBottomSheetViewModel: ViewModel() {

    private var flag: String? = null

    private var flagStatus: Int? = null

    private var availableQuantity: Int? = null

    private var totalNumber: Int? = null

    private var pharmacyAddressesDetails: PharmacyAddressesDetailsModel? = null

    private var arrayListOperatingModesTimeFrom: ArrayList<String>? = null

    private var arrayListOperatingModesTimeBefore: ArrayList<String>? = null

    fun initValues(
        flag: String?,
        flagStatus: Int?,
        pharmacyAddressesDetails: PharmacyAddressesDetailsModel?,
        arrayListOperatingModesTimeFrom: ArrayList<String>?,
        arrayListOperatingModesTimeBefore: ArrayList<String>?,
        availableQuantity: Int?,
        totalNumber: Int?,
    ){
        this.flag = flag?: FLAG_ALL_PHARMACIES
        this.flagStatus = flagStatus
        this.pharmacyAddressesDetails = pharmacyAddressesDetails
        this.arrayListOperatingModesTimeFrom = arrayListOperatingModesTimeFrom
        this.arrayListOperatingModesTimeBefore = arrayListOperatingModesTimeBefore
        this.availableQuantity = availableQuantity
        this.totalNumber = totalNumber
    }

    fun installMap(block: (Boolean,Boolean,Boolean) -> Unit){
        try {
            when (flag) {
                FLAG_ALL_PHARMACIES -> {
                    block(false,false,false)
                }
                FLAG_CURRENT_PRODUCT -> {
                    block(true,false,false)
                }
                FLAG_SELECT_ADDRESS_FOR_ORDER_MAKING -> {
                    val isEnabledButton = availableQuantity != 0
                    block(true,true,isEnabledButton)
                }
                else -> throw NullPointerException()
            }
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    fun installUI(block: (Int,Int,Int,Int,Int) -> Unit) {
        try {
            var colorInt = 0
            var textInt = 0
            var image = 0
            when(flagStatus){
                MapViewModel.FLAG_IN_STOCK -> {
                    colorInt = R.color.green800
                    textInt = R.string.all_products_are_in_stock
                    image = R.drawable.ic_check_circle
                }
                MapViewModel.FLAG_OUT_OF_STOCK -> {
                    colorInt = R.color.red700
                    textInt = R.string.out_of_stock
                    image = R.drawable.ic_remove_circle
                }
                MapViewModel.FLAG_WARNING -> {
                    colorInt = R.color.warning
                    textInt = R.string.available_out_of
                    image = R.drawable.ic_warning
                }
                else -> throw IllegalArgumentException()
            }
            block(colorInt,textInt,image,availableQuantity!!,totalNumber!!)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    fun chooseAddress(block: (Int) -> Unit){
        try {
            block(pharmacyAddressesDetails!!.pharmacyAddressesModel.addressId)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    /**
     * Заполнение режима работы, выбранной аптеки.
     *
     * Параметры:
     * [arrayListOperatingModesTimeFrom] - список с временем начала работы;
     * [arrayListOperatingModesTimeBefore] - список с временем окончания работы.
     */
    fun fillingOperatingMode(block: (String,String,String,String,String,String,String,) -> Unit) {
        try {
            var textMo = ""
            var textTu = ""
            var textWe = ""
            var textTh = ""
            var textFr = ""
            var textSa = ""
            var textSu = ""
            for (day in 1..7) {

                val index = day-1

                val textTimeFrom = arrayListOperatingModesTimeFrom!![index].toHourAndMinutes()
                val textTimeBefore = arrayListOperatingModesTimeBefore!![index].toHourAndMinutes()

                val fullTime = "$textTimeFrom-$textTimeBefore"

                when(day) {
                    1 -> textMo = fullTime
                    2 -> textTu = fullTime
                    3 -> textWe = fullTime
                    4 -> textTh = fullTime
                    5 -> textFr = fullTime
                    6 -> textSa = fullTime
                    7 -> textSu = fullTime

                }

            }
            block(textMo,textTu,textWe,textTh,textFr,textSa,textSu)
        }
        catch (e: Exception){
            Log.e("TAG",e.stackTraceToString())
        }
    }

    /**
     * Преобразование сторки времени к формату ЧЧ:ММ.
     */
    private fun String.toHourAndMinutes(): String{
        var time = ""
        var counter = 0
        this.forEach {
            if (it == ':') counter++
            if (counter <= 1) time += it
        }
        return time
    }
}