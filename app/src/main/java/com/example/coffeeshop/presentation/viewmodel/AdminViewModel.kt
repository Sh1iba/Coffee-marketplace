package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.AdminCourierResponse
import com.example.coffeeshop.data.remote.response.AdminUserResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
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

    private val _pendingProducts = MutableStateFlow<List<ProductResponse>>(emptyList())
    val pendingProducts: StateFlow<List<ProductResponse>> = _pendingProducts

    private val _sellerProducts = MutableStateFlow<Map<Long, List<ProductResponse>>>(emptyMap())
    val sellerProducts: StateFlow<Map<Long, List<ProductResponse>>> = _sellerProducts

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

    fun loadPendingProducts() {
        viewModelScope.launch {
            _pendingProducts.value = adminRepository.getPendingProducts()
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

    fun loadSellerProducts(sellerId: Long) {
        viewModelScope.launch {
            val products = adminRepository.getSellerProducts(sellerId)
            _sellerProducts.value = _sellerProducts.value + (sellerId to products)
        }
    }

    fun approveProduct(sellerId: Long, productId: Int) {
        viewModelScope.launch {
            if (adminRepository.approveProduct(productId)) {
                updateProductStatus(sellerId, productId, "APPROVED")
                _pendingProducts.value = _pendingProducts.value.filter { it.id != productId }
            } else {
                _error.value = "Не удалось одобрить товар"
            }
        }
    }

    fun rejectProduct(sellerId: Long, productId: Int, reason: String) {
        viewModelScope.launch {
            if (adminRepository.rejectProduct(productId, reason)) {
                updateProductStatus(sellerId, productId, "REJECTED", reason)
                _pendingProducts.value = _pendingProducts.value.filter { it.id != productId }
            } else {
                _error.value = "Не удалось отклонить товар"
            }
        }
    }

    fun deleteProduct(sellerId: Long, productId: Int) {
        viewModelScope.launch {
            if (adminRepository.deleteProduct(productId)) {
                val updated = _sellerProducts.value[sellerId]?.filter { it.id != productId } ?: emptyList()
                _sellerProducts.value = _sellerProducts.value + (sellerId to updated)
                _pendingProducts.value = _pendingProducts.value.filter { it.id != productId }
            } else {
                _error.value = "Не удалось удалить товар"
            }
        }
    }

    private fun updateProductStatus(sellerId: Long, productId: Int, status: String, reason: String? = null) {
        val updated = _sellerProducts.value[sellerId]?.map {
            if (it.id == productId) it.copy(status = status, rejectionReason = reason) else it
        } ?: emptyList()
        _sellerProducts.value = _sellerProducts.value + (sellerId to updated)
    }

    fun clearError() { _error.value = null }
}
