package com.example.coffeeshop.data.remote.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class OrderResponse(
    val id: Long,
    val totalAmount: BigDecimal,
    val deliveryFee: BigDecimal,
    val deliveryAddress: String?,
    val branchId: Long?,
    val branchName: String?,
    val courierId: Long?,
    val deliveryType: String = "DELIVERY",
    val orderDate: String,
    val status: String = "PENDING",
    val items: List<OrderItemResponse>
)

data class OrderItemResponse(
    val id: Long,
    // JSON: "productName" → Kotlin: coffeeName (чтобы не менять экраны)
    @SerializedName("productName") val coffeeName: String,
    val selectedSize: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val totalPrice: BigDecimal
)

data class OrdersHistoryResponse(
    val orders: List<OrderResponse>
)
