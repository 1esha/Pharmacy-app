package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pharmacyapp.ToolbarDataModel

class ToolbarViewModel: ViewModel() {

    private val _toolbarLiveData = MutableLiveData<ToolbarDataModel>()
    val toolbarLiveData: LiveData<ToolbarDataModel> = _toolbarLiveData

    fun setToolbarData(toolbarDataModel: ToolbarDataModel) {
        _toolbarLiveData.value = toolbarDataModel
    }

}