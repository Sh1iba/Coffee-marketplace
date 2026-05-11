package com.example.coffeeshop.data.remote.response

data class AdminUserResponse(
    val id: Long,
    val email: String,
    val name: String,
    val role: String
)
