package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.remote.response.OrderResponse
import com.example.coffeeshop.data.repository.OrderRepository
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
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderResponse>>(emptyList())
    val orders: StateFlow<List<OrderResponse>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var pollingJob: Job? = null

    fun loadOrderHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _orders.value = orderRepository.getOrderHistory()
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

    fun clearError() { _error.value = null }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }
}
