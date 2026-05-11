package com.example.coffeeshop.domain

import java.math.BigDecimal

data class BranchRequest(
    val name: String,
    val address: String,
    val city: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryFee: BigDecimal = BigDecimal.ZERO,
    val minOrderAmount: BigDecimal = BigDecimal.ZERO,
    val workingHours: String? = null,
    val managerEmail: String? = null,
    val managerPassword: String? = null
)
