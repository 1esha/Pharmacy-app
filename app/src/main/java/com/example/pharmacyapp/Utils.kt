package com.example.pharmacyapp

const val KEY_IS_EXIST = "KEY_IS_EXIST"
const val KEY_IS_INIT = "KEY_IS_INIT"
const val KEY_ENTER_THE_DATA = "KEY_ENTER_THE_DATA"
const val NAME_SHARED_PREFERENCES = "NAME_SHARED_PREFERENCES"

fun getResultByStatus(status: Int){
    when(status){
        in 100..199 -> {}
        in 200..299 -> {}
        in 300..399 -> {}
        in 400..499 -> {}
        in 500..599 -> {}
    }
}

