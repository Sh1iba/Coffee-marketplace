package com.example.coffeeshop.data.repository

import com.example.coffeeshop.data.remote.api.ApiService
import com.example.coffeeshop.data.remote.response.AdminCourierResponse
import com.example.coffeeshop.data.remote.response.AdminUserResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.domain.RejectSellerRequest
import com.example.coffeeshop.domain.RoleChangeRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllUsers(): List<AdminUserResponse> = try {
        apiService.getAdminUsers().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun changeUserRole(userId: Long, role: String): Boolean = try {
        apiService.changeUserRole(userId, RoleChangeRequest(role)).isSuccessful
    } catch (e: Exception) { false }

    suspend fun getAllSellers(): List<SellerResponse> = try {
        apiService.getAdminAllSellers().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun getPendingSellers(): List<SellerResponse> = try {
        apiService.getAdminPendingSellers().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun approveSeller(sellerId: Long): Boolean = try {
        apiService.approveSeller(sellerId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun rejectSeller(sellerId: Long, reason: String): Boolean = try {
        apiService.rejectSeller(sellerId, RejectSellerRequest(reason)).isSuccessful
    } catch (e: Exception) { false }

    suspend fun activateSeller(sellerId: Long): Boolean = try {
        apiService.activateSeller(sellerId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun deactivateSeller(sellerId: Long): Boolean = try {
        apiService.deactivateSeller(sellerId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun getAllCouriers(): List<AdminCourierResponse> = try {
        apiService.getAdminCouriers().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun toggleCourier(courierId: Long): Boolean = try {
        apiService.toggleCourier(courierId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun removeCourier(courierId: Long): Boolean = try {
        apiService.removeCourier(courierId).isSuccessful
    } catch (e: Exception) { false }
}
