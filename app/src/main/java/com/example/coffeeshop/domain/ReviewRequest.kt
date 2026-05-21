package com.example.coffeeshop.domain

data class ReviewRequest(
    val orderId: Long,
    val rating: Int,
    val comment: String? = null
)
