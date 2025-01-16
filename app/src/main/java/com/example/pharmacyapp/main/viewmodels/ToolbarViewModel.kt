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

    fun installToolbar(toolbarSettingsModel: ToolbarSettingsModel) {
        _toolbarSettings.value = toolbarSettingsModel
    }

    fun installMenu(menuSettingsModel: MenuSettingsModel) {
        _menuSettings.value = menuSettingsModel
    }

    fun clearMenu() {
        _menuSettings.value = null
    }

}