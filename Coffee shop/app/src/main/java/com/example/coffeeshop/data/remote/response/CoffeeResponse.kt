package com.example.coffeeshop.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    val id: Int,
    @SerializedName("category") val type: ProductCategoryResponse,
    val name: String,
    val description: String,
    @SerializedName("variants") val sizes: List<ProductVariantResponse>,
    val imageName: String,
    val sellerId: Long? = null,
    val sellerName: String? = null
)

data class ProductVariantResponse(
    val size: String,
    val price: Float,
    val volume: String? = null
)
