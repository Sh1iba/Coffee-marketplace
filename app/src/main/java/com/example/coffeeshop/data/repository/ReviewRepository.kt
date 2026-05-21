package com.example.coffeeshop.data.repository

import com.example.coffeeshop.data.remote.api.ApiService
import com.example.coffeeshop.data.remote.response.ReviewResponse
import com.example.coffeeshop.domain.ReviewRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getSellerReviews(sellerId: Long): List<ReviewResponse> {
        return try {
            val r = apiService.getSellerReviews(sellerId)
            if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun submitReview(orderId: Long, rating: Int, comment: String?): ReviewResponse? {
        return try {
            val r = apiService.submitReview(ReviewRequest(orderId, rating, comment))
            if (r.isSuccessful) r.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMyReviews(): List<ReviewResponse> {
        return try {
            val r = apiService.getMyReviews()
            if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
