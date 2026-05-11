package com.example.coffeeshop.data.remote.response

data class CartItemResponse(
    val id: Int,
    val name: String,
    val selectedSize: String,
    val price: Float,
    val quantity: Int,
    val totalPrice: Float,
    val imageUrl: String = "",
    val sellerId: Long? = null
)

data class CartSummaryResponse(
    val items: List<CartItemResponse>,
    val totalItems: Int,
    val totalPrice: Float
)
