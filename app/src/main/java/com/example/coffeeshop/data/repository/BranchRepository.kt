package com.example.coffeeshop.data.repository

import com.example.coffeeshop.data.remote.api.ApiService
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.domain.BranchRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BranchRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getBranchesBySeller(sellerId: Long): List<BranchResponse> = try {
        val r = apiService.getBranchesBySeller(sellerId)
        if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun getMyBranches(): List<BranchResponse> = try {
        val r = apiService.getMyBranches()
        if (r.isSuccessful) r.body() ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun createBranch(request: BranchRequest): BranchResponse? = try {
        val r = apiService.createBranch(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun updateBranch(branchId: Long, request: BranchRequest): BranchResponse? = try {
        val r = apiService.updateBranch(branchId, request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun toggleBranch(branchId: Long): BranchResponse? = try {
        val r = apiService.toggleBranch(branchId)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }
}
