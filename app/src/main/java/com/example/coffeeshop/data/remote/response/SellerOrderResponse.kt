package com.example.coffeeshop.data.remote.response

import java.math.BigDecimal

data class SellerOrderResponse(
    val orderId: Long,
    val orderDate: String,
    val status: String,
    val deliveryAddress: String?,
    val itemsTotal: BigDecimal,
    val items: List<OrderItemResponse>
)
