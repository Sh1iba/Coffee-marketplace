package com.example.coffeeshop.presentation.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.repository.FavoriteRepository
import com.example.coffeeshop.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

@HiltViewModel
class FavoriteCoffeeViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _favoriteCoffees = MutableStateFlow<List<Pair<ProductResponse, String>>>(emptyList())
    val favoriteCoffees: StateFlow<List<Pair<ProductResponse, String>>> = _favoriteCoffees.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _imageMap = mutableStateMapOf<Int, ByteArray?>()

    private val _scrollState = MutableStateFlow(0)
    val scrollState: StateFlow<Int> = _scrollState.asStateFlow()

    fun saveScrollPosition(position: Int) { _scrollState.value = position }

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val favorites = favoriteRepository.getFavorites()
                val allProducts = productRepository.getAllProducts()

                val favoritePairs = favorites.mapNotNull { favorite ->
                    allProducts.find { it.id == favorite.id }?.let { it to favorite.selectedSize }
                }
                _favoriteCoffees.value = favoritePairs

                favoritePairs.forEach { (product, _) ->
                    if (product.imageName.isNotEmpty() && !_imageMap.containsKey(product.id)) {
                        _imageMap[product.id] = productRepository.getProductImage(product.imageName)
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeFromFavorites(productId: Int, size: String) {
        viewModelScope.launch {
            try {
                val success = favoriteRepository.removeFromFavorites(productId, size)
                if (success) {
                    _favoriteCoffees.value = _favoriteCoffees.value.filter { (coffee, savedSize) ->
                        coffee.id != productId || savedSize != size
                    }
                    _imageMap.remove(productId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getImageForProduct(productId: Int): ByteArray? = _imageMap[productId]

    fun clearError() { _error.value = null }

    fun getPriceForSavedSize(coffee: ProductResponse, savedSize: String): Float {
        return coffee.sizes.find { it.size == savedSize }?.price ?: getDefaultPrice(coffee)
    }

    fun getDefaultPrice(coffee: ProductResponse): Float {
        return coffee.sizes.find { it.size == "M" }?.price ?: coffee.sizes.firstOrNull()?.price ?: 0f
    }

    fun encodeSizesForNavigation(coffee: ProductResponse): String {
        return URLEncoder.encode(
            coffee.sizes.joinToString(",") { "${it.size}:${it.price}:${it.volume ?: ""}" },
            "UTF-8"
        )
    }
}
