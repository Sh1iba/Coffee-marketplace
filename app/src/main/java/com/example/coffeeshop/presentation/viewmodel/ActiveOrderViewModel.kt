package com.example.coffeeshop.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coffeeshop.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActiveOrderState(
    val status: String = "PENDING",
    val statusLabel: String = "Ожидает подтверждения",
    val progress: Float = 0.05f,
    val isOrderDelivered: Boolean = false,
    val isOrderCancelled: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ActiveOrderEvent {
    object NavigateToHome : ActiveOrderEvent()
}

@HiltViewModel
class ActiveOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val orderId: Long = savedStateHandle.get<Long>("orderId") ?: 0L

    private val _state = MutableStateFlow(ActiveOrderState())
    val state: StateFlow<ActiveOrderState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ActiveOrderEvent>()
    val events: SharedFlow<ActiveOrderEvent> = _events.asSharedFlow()

    init {
        if (orderId > 0L) startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                try {
                    val order = orderRepository.getOrderDetails(orderId)
                    if (order != null) {
                        val delivered = order.status == "DELIVERED"
                        val cancelled = order.status == "CANCELLED"
                        _state.update {
                            it.copy(
                                status = order.status,
                                statusLabel = statusToLabel(order.status),
                                progress = statusToProgress(order.status),
                                isOrderDelivered = delivered,
                                isOrderCancelled = cancelled,
                                isLoading = false,
                                error = null
                            )
                        }
                        if (delivered || cancelled) break
                    } else {
                        _state.update { it.copy(isLoading = false) }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = "Ошибка соединения") }
                }
                delay(5000)
            }
        }
    }

    fun onEvent(event: ActiveOrderEvent) {
        viewModelScope.launch {
            when (event) {
                is ActiveOrderEvent.NavigateToHome -> _events.emit(event)
            }
        }
    }

    private fun statusToLabel(status: String) = when (status) {
        "PENDING"            -> "Ожидает подтверждения"
        "CONFIRMED"          -> "Заказ подтверждён"
        "COOKING"            -> "Готовится..."
        "READY_FOR_PICKUP"   -> "Готово! Ожидает курьера"
        "PICKED_UP"          -> "Курьер забрал заказ"
        "DELIVERING"         -> "В пути к вам"
        "DELIVERED"          -> "Доставлен!"
        "CANCELLED"          -> "Заказ отменён"
        else                 -> status
    }

    private fun statusToProgress(status: String) = when (status) {
        "PENDING"            -> 0.1f
        "CONFIRMED"          -> 0.25f
        "COOKING"            -> 0.45f
        "READY_FOR_PICKUP"   -> 0.65f
        "PICKED_UP"          -> 0.75f
        "DELIVERING"         -> 0.9f
        "DELIVERED"          -> 1.0f
        else                 -> 0f
    }
}
