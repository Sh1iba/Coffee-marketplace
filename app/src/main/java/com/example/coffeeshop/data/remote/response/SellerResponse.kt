package com.example.coffeeshop.data.remote.response

data class SellerResponse(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    @com.google.gson.annotations.SerializedName("logoUrl") val logoImage: String?,
    val rating: Double,
    val isActive: Boolean,
    val phone: String? = null,
    val website: String? = null,
    val ownerId: Long,
    val ownerName: String,
    val status: String = "APPROVED",
    val rejectionReason: String? = null
)
