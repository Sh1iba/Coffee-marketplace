package com.example.coffeeshop.presentation.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.data.repository.ProductRepository
import com.example.coffeeshop.data.repository.SellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val sellerRepository: SellerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _imageCache = mutableStateMapOf<String, ByteArray?>()
    val imageCache: Map<String, ByteArray?> get() = _imageCache

    private val _lastSearchQuery = MutableStateFlow("")
    private val _lastSelectedType = MutableStateFlow<String?>(null)

    private val _showSizeDialog = MutableStateFlow<ProductResponse?>(null)
    val showSizeDialog: StateFlow<ProductResponse?> = _showSizeDialog.asStateFlow()

    private val _sellers = MutableStateFlow<List<SellerResponse>>(emptyList())
    val sellers: StateFlow<List<SellerResponse>> = _sellers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun showSizeSelectionDialog(product: ProductResponse) { _showSizeDialog.value = product }
    fun hideSizeSelectionDialog() { _showSizeDialog.value = null }

    fun loadCoffeeData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val products = productRepository.getAllProducts()
                val types = productRepository.getAllCategories()
                val sellers = sellerRepository.getAllSellers()
                _sellers.value = sellers
                if (products.isEmpty() && types.isEmpty()) {
                    _error.value = "Не удалось загрузить данные. Проверьте подключение."
                } else {
                    val categoryTypes = listOf("Все товары") + types.map { it.type }
                    val typeMapping = types.associate { it.type to it.id }
                    _uiState.update {
                        it.copy(
                            allProducts = products,
                            filteredProducts = applySavedFilters(products),
                            productTypes = categoryTypes,
                            productTypeMapping = typeMapping
                        )
                    }
                    loadProductImages(products)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    internal fun encodeSizesForNavigation(product: ProductResponse): String {
        return URLEncoder.encode(
            product.sizes.joinToString(",") { "${it.size}:${it.price}:${it.volume ?: ""}" },
            "UTF-8"
        )
    }

    private fun applySavedFilters(products: List<ProductResponse>): List<ProductResponse> {
        var filtered = products
        _lastSelectedType.value?.let { typeName ->
            if (typeName != "Все товары") {
                val typeId = _uiState.value.productTypeMapping[typeName]
                if (typeId != null) filtered = filtered.filter { it.type.id == typeId }
            }
        }
        if (_lastSearchQuery.value.isNotBlank()) {
            filtered = filtered.filter { p ->
                p.name.contains(_lastSearchQuery.value, ignoreCase = true) ||
                        p.type.type.contains(_lastSearchQuery.value, ignoreCase = true) ||
                        p.description.contains(_lastSearchQuery.value, ignoreCase = true)
            }
        }
        return filtered
    }

    fun getDefaultPrice(product: ProductResponse): Float {
        return product.sizes.find { it.size == "M" }?.price ?: product.sizes.firstOrNull()?.price ?: 0f
    }

    fun getAvailableSizes(product: ProductResponse): List<String> = product.sizes.map { it.size }

    fun getPriceBySize(product: ProductResponse, size: String): Float {
        return product.sizes.find { it.size == size }?.price ?: getDefaultPrice(product)
    }

    private fun loadProductImages(products: List<ProductResponse>) {
        viewModelScope.launch {
            products.forEach { p ->
                if (!_imageCache.containsKey(p.imageName)) {
                    _imageCache[p.imageName] = productRepository.getProductImage(p.imageName)
                }
            }
        }
    }

    fun onCoffeeTypeSelected(typeName: String) {
        _lastSelectedType.value = typeName
        val currentState = _uiState.value
        if (typeName == "Все товары") {
            _uiState.update {
                it.copy(
                    selectedTypeName = typeName,
                    selectedTypeId = null,
                    filteredProducts = if (it.isSearching) it.searchResults else it.allProducts
                )
            }
        } else {
            val typeId = currentState.productTypeMapping[typeName]
            val filtered = if (currentState.isSearching) {
                currentState.searchResults.filter { it.type.id == typeId }
            } else {
                currentState.allProducts.filter { it.type.id == typeId }
            }
            _uiState.update { it.copy(selectedTypeName = typeName, selectedTypeId = typeId, filteredProducts = filtered) }
        }
    }

    fun searchCoffee(query: String) {
        _lastSearchQuery.value = query
        val currentState = _uiState.value
        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    isSearching = false,
                    searchResults = emptyList(),
                    filteredProducts = if (it.selectedTypeId == null) it.allProducts
                    else it.allProducts.filter { p -> p.type.id == it.selectedTypeId }
                )
            }
            return
        }
        val searchResults = currentState.allProducts.filter { p ->
            p.name.contains(query, ignoreCase = true) ||
                    p.type.type.contains(query, ignoreCase = true) ||
                    p.description.contains(query, ignoreCase = true)
        }
        val finalFiltered = if (currentState.selectedTypeId != null) {
            searchResults.filter { it.type.id == currentState.selectedTypeId }
        } else searchResults

        _uiState.update { it.copy(isSearching = true, searchResults = searchResults, filteredProducts = finalFiltered) }
    }

    fun getCurrentSelectedType(): String? = _lastSelectedType.value
    fun getCurrentSearchQuery(): String = _lastSearchQuery.value
    fun clearSavedState() {
        _lastSearchQuery.value = ""
        _lastSelectedType.value = null
    }
}

data class HomeUiState(
    val allProducts: List<ProductResponse> = emptyList(),
    val filteredProducts: List<ProductResponse> = emptyList(),
    val productTypes: List<String> = emptyList(),
    val productTypeMapping: Map<String, Int> = emptyMap(),
    val selectedTypeId: Int? = null,
    val selectedTypeName: String = "Все товары",
    val searchResults: List<ProductResponse> = emptyList(),
    val isSearching: Boolean = false
)
