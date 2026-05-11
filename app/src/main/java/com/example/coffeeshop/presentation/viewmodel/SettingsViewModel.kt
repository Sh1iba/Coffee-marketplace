package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.managers.PrefsManager
import com.example.coffeeshop.data.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsManager: PrefsManager,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _darkModeState = MutableStateFlow(prefsManager.getBoolean(PrefsManager.KEY_DARK_MODE, false))
    val darkModeState: StateFlow<Boolean> = _darkModeState

    private val _notificationsState = MutableStateFlow(prefsManager.getBoolean(PrefsManager.KEY_NOTIFICATIONS, true))
    val notificationsState: StateFlow<Boolean> = _notificationsState

    val userName: StateFlow<String> = MutableStateFlow(prefsManager.getName() ?: "Пользователь")
    val userEmail: StateFlow<String> = MutableStateFlow(prefsManager.getEmail() ?: "email@example.com")
    val isSeller: StateFlow<Boolean> = MutableStateFlow(prefsManager.isSeller())

    private val _shopStatus = MutableStateFlow<String?>(null)
    val shopStatus: StateFlow<String?> = _shopStatus

    init {
        if (prefsManager.isSeller()) {
            viewModelScope.launch {
                _shopStatus.value = sellerRepository.getMyShop()?.status
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.saveBoolean(PrefsManager.KEY_DARK_MODE, enabled)
            _darkModeState.value = enabled
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            prefsManager.saveBoolean(PrefsManager.KEY_NOTIFICATIONS, enabled)
            _notificationsState.value = enabled
        }
    }

    fun logout() {
        prefsManager.logout()
    }
}
