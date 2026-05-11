package com.example.coffeeshop.data.remote.response

import java.math.BigDecimal

data class BranchResponse(
    val id: Long,
    val sellerId: Long,
    val sellerName: String,
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?,
    val deliveryFee: BigDecimal,
    val minOrderAmount: BigDecimal,
    val workingHours: String?,
    val isActive: Boolean,
    val managerEmail: String?
)
