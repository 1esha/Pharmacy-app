package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pharmacyapp.MenuSettingsModel
import com.example.pharmacyapp.ToolbarSettingsModel

class ToolbarViewModel: ViewModel() {

    private val _toolbarSettings = MutableLiveData<ToolbarSettingsModel>()
    val toolbarSettings: LiveData<ToolbarSettingsModel> = _toolbarSettings

    private val _menuSettings = MutableLiveData<MenuSettingsModel?>()
    val menuSettings: LiveData<MenuSettingsModel?> = _menuSettings

    fun setToolbarSettings(toolbarSettingsModel: ToolbarSettingsModel) {
        _toolbarSettings.value = toolbarSettingsModel
    }

    fun setMenuSettings(menuSettingsModel: MenuSettingsModel? = null) {
        _menuSettings.value = menuSettingsModel
    }

}