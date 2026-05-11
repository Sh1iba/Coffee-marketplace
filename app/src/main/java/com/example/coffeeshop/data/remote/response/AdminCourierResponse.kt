package com.example.coffeeshop.data.remote.response

import com.google.gson.annotations.SerializedName

data class AdminCourierResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    @SerializedName("isAvailable") val isAvailable: Boolean,
    val latitude: Double?,
    val longitude: Double?
)
