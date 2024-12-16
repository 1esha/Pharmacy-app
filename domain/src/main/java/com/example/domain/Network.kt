package com.example.domain

class Network() {

    fun checkNetworkStatus(
        isNetworkStatus: Boolean,
        connectionListener: () -> Unit,
        disconnectionListener: () -> Unit
    ){
        if (isNetworkStatus){
            connectionListener()
        }
        else{
            disconnectionListener()
        }

    }
}