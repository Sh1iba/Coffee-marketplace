package com.example.coffeeshop.data.remote.response

data class ReviewResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val sellerId: Long,
    val sellerName: String,
    val orderId: Long,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)
