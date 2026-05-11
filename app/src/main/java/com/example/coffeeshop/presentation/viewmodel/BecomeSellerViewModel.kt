package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.managers.PrefsManager
import com.example.coffeeshop.data.repository.SellerRepository
import com.example.coffeeshop.domain.SellerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class BecomeSellerViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isBannerUploading = MutableStateFlow(false)
    val isBannerUploading: StateFlow<Boolean> = _isBannerUploading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _success = MutableStateFlow(false)
    val success: StateFlow<Boolean> = _success

    fun uploadBanner(file: MultipartBody.Part, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isBannerUploading.value = true
            val url = sellerRepository.uploadProductImage(file)
            _isBannerUploading.value = false
            onResult(url)
        }
    }

    fun register(name: String, description: String, category: String, phone: String, website: String?, logoUrl: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = sellerRepository.becomeSeller(
                SellerRequest(
                    name = name.trim(),
                    description = description.trim(),
                    category = category.trim(),
                    phone = phone.trim(),
                    website = website?.trim()?.ifBlank { null },
                    logoImage = logoUrl
                )
            )
            if (result != null) {
                prefsManager.saveRole("SELLER")
                _success.value = true
            } else {
                _error.value = "Не удалось зарегистрироваться. Попробуйте ещё раз"
            }
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}
