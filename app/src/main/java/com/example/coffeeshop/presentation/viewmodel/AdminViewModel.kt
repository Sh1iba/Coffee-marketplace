package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.AdminCourierResponse
import com.example.coffeeshop.data.remote.response.AdminUserResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _pendingSellers = MutableStateFlow<List<SellerResponse>>(emptyList())
    val pendingSellers: StateFlow<List<SellerResponse>> = _pendingSellers

    private val _allSellers = MutableStateFlow<List<SellerResponse>>(emptyList())
    val allSellers: StateFlow<List<SellerResponse>> = _allSellers

    private val _allUsers = MutableStateFlow<List<AdminUserResponse>>(emptyList())
    val allUsers: StateFlow<List<AdminUserResponse>> = _allUsers

    private val _couriers = MutableStateFlow<List<AdminCourierResponse>>(emptyList())
    val couriers: StateFlow<List<AdminCourierResponse>> = _couriers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadPendingSellers() {
        viewModelScope.launch {
            _isLoading.value = true
            _pendingSellers.value = adminRepository.getPendingSellers()
            _isLoading.value = false
        }
    }

    fun loadAllSellers() {
        viewModelScope.launch {
            _isLoading.value = true
            _allSellers.value = adminRepository.getAllSellers()
            _isLoading.value = false
        }
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            _allUsers.value = adminRepository.getAllUsers()
            _isLoading.value = false
        }
    }

    fun approveSeller(sellerId: Long) {
        viewModelScope.launch {
            if (adminRepository.approveSeller(sellerId)) {
                _pendingSellers.value = _pendingSellers.value.filter { it.id != sellerId }
                _allSellers.value = _allSellers.value.map {
                    if (it.id == sellerId) it.copy(status = "APPROVED") else it
                }
            } else {
                _error.value = "Не удалось одобрить магазин"
            }
        }
    }

    fun rejectSeller(sellerId: Long, reason: String) {
        viewModelScope.launch {
            if (adminRepository.rejectSeller(sellerId, reason)) {
                _pendingSellers.value = _pendingSellers.value.filter { it.id != sellerId }
                _allSellers.value = _allSellers.value.map {
                    if (it.id == sellerId) it.copy(status = "REJECTED", rejectionReason = reason) else it
                }
            } else {
                _error.value = "Не удалось отклонить магазин"
            }
        }
    }

    fun toggleSellerActive(seller: SellerResponse) {
        viewModelScope.launch {
            val ok = if (seller.isActive) {
                adminRepository.deactivateSeller(seller.id)
            } else {
                adminRepository.activateSeller(seller.id)
            }
            if (ok) {
                _allSellers.value = _allSellers.value.map {
                    if (it.id == seller.id) it.copy(isActive = !seller.isActive) else it
                }
            } else {
                _error.value = "Не удалось изменить статус магазина"
            }
        }
    }

    fun clearError() { _error.value = null }
}
