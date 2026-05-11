package com.example.coffeeshop.domain

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class OrderRequest(
    val branchId: Long? = null,
    val deliveryAddress: String? = null,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val deliveryType: String = "DELIVERY",
    val items: List<OrderCartItem>
)

data class OrderCartItem(
    @SerializedName("productId") val coffeeId: Int,
    val selectedSize: String
)
