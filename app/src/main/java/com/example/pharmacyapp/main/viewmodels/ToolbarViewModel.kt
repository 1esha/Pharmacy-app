package com.example.pharmacyapp.main.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pharmacyapp.ToolbarSettingsModel

class ToolbarViewModel: ViewModel() {

    private val _toolbarSettings = MutableLiveData<ToolbarSettingsModel>()
    val toolbarSettings: LiveData<ToolbarSettingsModel> = _toolbarSettings

    private val _menu = MutableLiveData<Int?>()
    val menu: LiveData<Int?> = _menu

    private val _menuClickListener = MutableLiveData<((Int) -> Unit)?>()
    val menuClickListener: LiveData<((Int) -> Unit)?> = _menuClickListener

    private val _isInstallSearchBar = MutableLiveData<Boolean>(false)
    val isInstallSearchBar: LiveData<Boolean> = _isInstallSearchBar

    private val _isVisibleToolbar = MutableLiveData<Boolean>(true)
    val isVisibleToolbar: LiveData<Boolean> = _isVisibleToolbar

    fun installToolbar(
        isVisibleToolbar: Boolean = true,
        isInstallSearchBar: Boolean = false,
        toolbarSettingsModel: ToolbarSettingsModel
    ) {
        _isVisibleToolbar.value = isVisibleToolbar
        _isInstallSearchBar.value = isInstallSearchBar
        _toolbarSettings.value = toolbarSettingsModel
    }

    fun inflateMenu(menu: Int) {
        _menu.value = menu
    }

    fun setMenuClickListener(listener: (Int) -> Unit) {
        _menuClickListener.value = listener
    }

    fun clearMenu() {
        _menu.value = null
        _menuClickListener.value = null
    }

}