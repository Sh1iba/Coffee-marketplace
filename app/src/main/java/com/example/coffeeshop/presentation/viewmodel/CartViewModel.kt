package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.CartSummaryResponse
import com.example.coffeeshop.data.remote.response.CartItemResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.data.repository.CartRepository
import com.example.coffeeshop.data.repository.ProductRepository
import com.example.coffeeshop.data.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _cartSummary = MutableStateFlow<CartSummaryResponse?>(null)
    val cartSummary: StateFlow<CartSummaryResponse?> = _cartSummary.asStateFlow()

    val cartItems: StateFlow<List<CartItemResponse>> = _cartSummary.map { summary ->
        summary?.items ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val totalPrice: StateFlow<Double> = _cartSummary.map { summary ->
        summary?.totalPrice?.toDouble() ?: 0.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0.0)

    val totalItems: StateFlow<Int> = _cartSummary.map { summary ->
        summary?.totalItems ?: 0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), 0)

    private val _sellers = MutableStateFlow<Map<Long, SellerResponse>>(emptyMap())
    val sellers: StateFlow<Map<Long, SellerResponse>> = _sellers.asStateFlow()

    suspend fun checkIfInCart(productId: Int, selectedSize: String): Boolean {
        return cartRepository.isInCart(productId, selectedSize)
    }

    fun loadCart() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val summary = cartRepository.getCart()
                _cartSummary.value = summary
                val uniqueSellerIds = summary.items.mapNotNull { it.sellerId }.distinct()
                val sellersMap = mutableMapOf<Long, SellerResponse>()
                uniqueSellerIds.forEach { id ->
                    sellerRepository.getSellerById(id)?.let { sellersMap[id] = it }
                }
                _sellers.value = sellersMap
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки корзины: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getFullProductData(productId: Int): ProductResponse? {
        return productRepository.getProductById(productId)
    }

    fun addToCart(productId: Int, selectedSize: String, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                val success = cartRepository.addToCart(productId, selectedSize, quantity)
                if (success) loadCart()
            } catch (e: Exception) {
                _error.value = "Ошибка добавления в корзину: ${e.message}"
            }
        }
    }

    fun updateQuantity(productId: Int, selectedSize: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                val success = cartRepository.updateQuantity(productId, selectedSize, newQuantity)
                if (success) loadCart()
            } catch (e: Exception) {
                _error.value = "Ошибка обновления количества: ${e.message}"
            }
        }
    }

    fun removeFromCart(productId: Int, selectedSize: String) {
        viewModelScope.launch {
            try {
                val success = cartRepository.removeFromCart(productId, selectedSize)
                if (success) loadCart()
            } catch (e: Exception) {
                _error.value = "Ошибка удаления из корзины: ${e.message}"
            }
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            try {
                val success = cartRepository.clearCart()
                if (success) {
                    _cartSummary.value = CartSummaryResponse(emptyList(), 0, 0f)
                    _sellers.value = emptyMap()
                }
            } catch (e: Exception) {
                _error.value = "Ошибка очистки корзины: ${e.message}"
            }
        }
    }

    fun clearError() { _error.value = null }
}
