package com.example.coffeeshop.domain

data class SellerRequest(
    val name: String,
    val description: String,
    val category: String,
    val phone: String,
    val website: String? = null,
    @com.google.gson.annotations.SerializedName("logoUrl") val logoImage: String? = null
)
