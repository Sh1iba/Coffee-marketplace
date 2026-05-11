package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.managers.PrefsManager
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.data.remote.response.CartItemResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.repository.BranchRepository
import com.example.coffeeshop.data.repository.OrderRepository
import com.example.coffeeshop.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderItem(
    val cartItem: CartItemResponse,
    val coffeeData: ProductResponse?
)

data class ParsedAddress(
    val mainAddress: String,
    val addressDetails: String
)

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val productRepository: ProductRepository,
    private val branchRepository: BranchRepository,
    private val prefsManager: PrefsManager
) : ViewModel() {

    private val _orderItems = MutableStateFlow<List<OrderItem>>(emptyList())
    val orderItems: StateFlow<List<OrderItem>> = _orderItems.asStateFlow()

    private val _branches = MutableStateFlow<List<BranchResponse>>(emptyList())
    val branches: StateFlow<List<BranchResponse>> = _branches.asStateFlow()

    private val _selectedBranch = MutableStateFlow<BranchResponse?>(null)
    val selectedBranch: StateFlow<BranchResponse?> = _selectedBranch.asStateFlow()

    private val _deliveryType = MutableStateFlow("DELIVERY")
    val deliveryType: StateFlow<String> = _deliveryType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addressNote = MutableStateFlow("")
    val addressNote: StateFlow<String> = _addressNote.asStateFlow()

    private val _showNoteDialog = MutableStateFlow(false)
    val showNoteDialog: StateFlow<Boolean> = _showNoteDialog.asStateFlow()

    private val _navigateToOrders = MutableStateFlow(false)
    val navigateToOrders: StateFlow<Boolean> = _navigateToOrders.asStateFlow()

    fun selectBranch(branch: BranchResponse) { _selectedBranch.value = branch }

    fun setDeliveryType(type: String) { _deliveryType.value = type }

    fun parseAddress(fullAddress: String): ParsedAddress {
        if (fullAddress.isEmpty()) return ParsedAddress("", "")
        return try {
            val parts = fullAddress.split(",")
            if (parts.size >= 2) ParsedAddress(parts[0].trim(), parts.subList(1, parts.size).joinToString(", ").trim())
            else ParsedAddress(fullAddress, "")
        } catch (e: Exception) {
            ParsedAddress(fullAddress, "")
        }
    }

    fun loadAddressNote(address: String) { _addressNote.value = prefsManager.getAddressNote(address) }

    fun saveAddressNote(address: String, note: String) {
        _addressNote.value = note
        if (address.isNotEmpty()) prefsManager.saveAddressNote(address, note)
    }

    fun clearAddressNote(address: String) {
        _addressNote.value = ""
        if (address.isNotEmpty()) prefsManager.clearAddressNote(address)
    }

    fun showNoteDialog() { _showNoteDialog.value = true }
    fun hideNoteDialog() { _showNoteDialog.value = false }

    fun createOrder(
        items: List<CartItemResponse>,
        address: String,
        note: String,
        totalPrice: Double,
        deliveryFee: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val fullAddress = if (note.isNotEmpty()) "$address ($note)" else address
                val orderId = orderRepository.createOrder(
                    items = items,
                    address = fullAddress,
                    deliveryFee = deliveryFee,
                    branchId = _selectedBranch.value?.id,
                    deliveryType = _deliveryType.value
                )
                if (orderId != null) {
                    _navigateToOrders.value = true
                    clearAddressNote(address)
                } else {
                    _error.value = "Ошибка при создании заказа"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка создания заказа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetAllNavigation() { _navigateToOrders.value = false }

    fun loadOrderItems(cartItems: List<CartItemResponse>) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val orderItemsList = cartItems.map { cartItem ->
                    OrderItem(
                        cartItem = cartItem,
                        coffeeData = productRepository.getProductById(cartItem.id)
                    )
                }
                _orderItems.value = orderItemsList

                val sellerId = cartItems.firstOrNull()?.sellerId
                if (sellerId != null) {
                    val branches = branchRepository.getBranchesBySeller(sellerId)
                    _branches.value = branches
                    if (_selectedBranch.value == null && branches.size == 1) {
                        _selectedBranch.value = branches.first()
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки данных заказа: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() { _error.value = null }
}
