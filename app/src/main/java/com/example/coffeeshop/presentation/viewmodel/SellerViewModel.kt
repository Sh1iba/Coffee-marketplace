package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerOrderResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.data.remote.response.ReviewResponse
import com.example.coffeeshop.data.repository.BranchRepository
import com.example.coffeeshop.data.repository.ReviewRepository
import com.example.coffeeshop.data.repository.SellerRepository
import com.example.coffeeshop.domain.BranchRequest
import com.example.coffeeshop.domain.ProductManageRequest
import com.example.coffeeshop.domain.SellerRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class SellerViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
    private val branchRepository: BranchRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _myShop = MutableStateFlow<SellerResponse?>(null)
    val myShop: StateFlow<SellerResponse?> = _myShop

    private val _myProducts = MutableStateFlow<List<ProductResponse>>(emptyList())
    val myProducts: StateFlow<List<ProductResponse>> = _myProducts

    private val _myOrders = MutableStateFlow<List<SellerOrderResponse>>(emptyList())
    val myOrders: StateFlow<List<SellerOrderResponse>> = _myOrders

    private val _myBranches = MutableStateFlow<List<BranchResponse>>(emptyList())
    val myBranches: StateFlow<List<BranchResponse>> = _myBranches

    private val _categories = MutableStateFlow<List<ProductCategoryResponse>>(emptyList())
    val categories: StateFlow<List<ProductCategoryResponse>> = _categories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _myReviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val myReviews: StateFlow<List<ReviewResponse>> = _myReviews

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        loadMyShop()
    }

    fun loadMyShop() {
        viewModelScope.launch {
            _isLoading.value = true
            _myShop.value = sellerRepository.getMyShop()
            _isLoading.value = false
        }
    }

    fun loadMyProducts() {
        viewModelScope.launch {
            _myProducts.value = sellerRepository.getMyProducts()
        }
    }

    fun loadMyOrders() {
        viewModelScope.launch {
            _myOrders.value = sellerRepository.getMySellerOrders()
        }
    }

    fun loadMyBranches() {
        viewModelScope.launch {
            _myBranches.value = branchRepository.getMyBranches()
        }
    }

    fun loadMyReviews() {
        viewModelScope.launch {
            val sellerId = _myShop.value?.id ?: return@launch
            _myReviews.value = reviewRepository.getSellerReviews(sellerId)
        }
    }

    fun createBranch(request: BranchRequest) {
        viewModelScope.launch {
            val result = branchRepository.createBranch(request)
            if (result != null) {
                _myBranches.value = _myBranches.value + result
            } else {
                _error.value = "Не удалось создать филиал"
            }
        }
    }

    fun updateBranch(branchId: Long, request: BranchRequest) {
        viewModelScope.launch {
            val result = branchRepository.updateBranch(branchId, request)
            if (result != null) {
                _myBranches.value = _myBranches.value.map { if (it.id == branchId) result else it }
            } else {
                _error.value = "Не удалось обновить филиал"
            }
        }
    }

    fun toggleBranch(branchId: Long) {
        viewModelScope.launch {
            val result = branchRepository.toggleBranch(branchId)
            if (result != null) {
                _myBranches.value = _myBranches.value.map { if (it.id == branchId) result else it }
            } else {
                _error.value = "Не удалось изменить статус филиала"
            }
        }
    }

    fun updateOrderStatus(orderId: Long, status: String) {
        viewModelScope.launch {
            val ok = sellerRepository.updateOrderStatus(orderId, status)
            if (ok) {
                _myOrders.value = _myOrders.value.map { order ->
                    if (order.orderId == orderId) order.copy(status = status) else order
                }
            } else {
                _error.value = "Не удалось обновить статус заказа"
            }
        }
    }

    fun createShop(request: SellerRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sellerRepository.createShop(request)
            if (result != null) {
                _myShop.value = result
                onSuccess()
            } else {
                _error.value = "Не удалось создать магазин"
            }
            _isLoading.value = false
        }
    }

    fun updateShop(request: SellerRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sellerRepository.updateMyShop(request)
            if (result != null) {
                _myShop.value = result
                onSuccess()
            } else {
                _error.value = "Не удалось обновить магазин"
            }
            _isLoading.value = false
        }
    }

    fun resubmitShop(request: SellerRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = sellerRepository.resubmitShop(request)
            if (result != null) {
                _myShop.value = result
                onSuccess()
            } else {
                _error.value = "Не удалось отправить заявку повторно"
            }
            _isLoading.value = false
        }
    }

    fun createProduct(request: ProductManageRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = sellerRepository.createProduct(request)
            if (result != null) {
                _myProducts.value = _myProducts.value + result
                onSuccess()
            } else {
                _error.value = "Не удалось создать товар"
            }
        }
    }

    fun updateProduct(productId: Int, request: ProductManageRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = sellerRepository.updateProduct(productId, request)
            if (result != null) {
                _myProducts.value = _myProducts.value.map { if (it.id == productId) result else it }
                onSuccess()
            } else {
                _error.value = "Не удалось обновить товар"
            }
        }
    }

    fun deleteProduct(productId: Int) {
        viewModelScope.launch {
            if (sellerRepository.deleteProduct(productId)) {
                _myProducts.value = _myProducts.value.filter { it.id != productId }
            } else {
                _error.value = "Не удалось удалить товар"
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            if (_categories.value.isEmpty()) {
                _categories.value = sellerRepository.getCategories()
            }
        }
    }

    fun uploadImage(file: MultipartBody.Part, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            _isUploading.value = true
            val name = sellerRepository.uploadProductImage(file)
            _isUploading.value = false
            onResult(name)
        }
    }

    fun clearError() { _error.value = null }
}
