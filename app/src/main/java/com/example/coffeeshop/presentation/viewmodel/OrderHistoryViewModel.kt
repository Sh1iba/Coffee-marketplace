package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.OrderResponse
import com.example.coffeeshop.data.repository.OrderRepository
import com.example.coffeeshop.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private val ACTIVE_STATUSES = setOf("PENDING", "CONFIRMED", "COOKING", "READY_FOR_PICKUP", "PICKED_UP", "DELIVERING")

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _reviewedOrderIds = MutableStateFlow<Set<Long>>(emptySet())
    val reviewedOrderIds: StateFlow<Set<Long>> = _reviewedOrderIds.asStateFlow()

    private val _reviewError = MutableStateFlow<String?>(null)
    val reviewError: StateFlow<String?> = _reviewError.asStateFlow()

    private var pollingJob: Job? = null

    fun loadOrderHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _orders.value = orderRepository.getOrderHistory()
                val myReviews = reviewRepository.getMyReviews()
                _reviewedOrderIds.value = myReviews.map { it.orderId }.toSet()
                restartPollingIfNeeded()
            } catch (e: Exception) {
                _error.value = e.message ?: "Неизвестная ошибка"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun restartPollingIfNeeded() {
        pollingJob?.cancel()
        if (_orders.value.none { it.status in ACTIVE_STATUSES }) return
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5_000)
                try {
                    val updated = orderRepository.getOrderHistory()
                    _orders.value = updated
                    if (updated.none { it.status in ACTIVE_STATUSES }) break
                } catch (_: Exception) { /* ignore, retry next tick */ }
            }
        }
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                if (orderRepository.cancelOrder(orderId)) {
                    _orders.value = _orders.value.map { o ->
                        if (o.id == orderId) o.copy(status = "CANCELLED") else o
                    }
                    restartPollingIfNeeded()
                } else {
                    _error.value = "Не удалось отменить заказ"
                }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun submitReview(orderId: Long, rating: Int, comment: String?) {
        viewModelScope.launch {
            val result = reviewRepository.submitReview(orderId, rating, comment)
            if (result != null) {
                _reviewedOrderIds.value = _reviewedOrderIds.value + orderId
            } else {
                _reviewError.value = "Не удалось отправить отзыв"
            }
        }
    }

    fun clearError() { _error.value = null }

    fun clearReviewError() { _reviewError.value = null }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
