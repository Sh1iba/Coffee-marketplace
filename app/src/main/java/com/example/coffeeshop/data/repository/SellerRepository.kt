package com.example.coffeeshop.data.repository

import com.example.coffeeshop.data.remote.api.ApiService
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerOrderResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.domain.OrderStatusRequest
import com.example.coffeeshop.domain.ProductManageRequest
import com.example.coffeeshop.domain.SellerRequest
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SellerRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getAllSellers(): List<SellerResponse> = try {
        apiService.getAllSellers().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun getSellerById(id: Long): SellerResponse? = try {
        val r = apiService.getSellerById(id)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun getMyShop(): SellerResponse? = try {
        val r = apiService.getMyShop()
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun becomeSeller(request: SellerRequest): SellerResponse? = try {
        val r = apiService.becomeSeller(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun createShop(request: SellerRequest): SellerResponse? = try {
        val r = apiService.createShop(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun updateMyShop(request: SellerRequest): SellerResponse? = try {
        val r = apiService.updateMyShop(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun resubmitShop(request: SellerRequest): SellerResponse? = try {
        val r = apiService.resubmitShop(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun getMyProducts(): List<ProductResponse> = try {
        apiService.getMyProducts().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun createProduct(request: ProductManageRequest): ProductResponse? = try {
        val r = apiService.createProduct(request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun updateProduct(productId: Int, request: ProductManageRequest): ProductResponse? = try {
        val r = apiService.updateProduct(productId, request)
        if (r.isSuccessful) r.body() else null
    } catch (e: Exception) { null }

    suspend fun deleteProduct(productId: Int): Boolean = try {
        apiService.deleteProduct(productId).isSuccessful
    } catch (e: Exception) { false }

    suspend fun getMySellerOrders(): List<SellerOrderResponse> = try {
        apiService.getMySellerOrders().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

    suspend fun uploadProductImage(file: MultipartBody.Part): String? = try {
        val r = apiService.uploadProductImage(file)
        if (r.isSuccessful) r.body()?.get("imageUrl") else null
    } catch (e: Exception) { null }

    suspend fun updateOrderStatus(orderId: Long, status: String): Boolean = try {
        apiService.updateSellerOrderStatus(orderId, OrderStatusRequest(status)).isSuccessful
    } catch (e: Exception) { false }

    suspend fun getCategories(): List<com.example.coffeeshop.data.remote.response.ProductCategoryResponse> = try {
        apiService.getAllCoffeeTypes().body() ?: emptyList()
    } catch (e: Exception) { emptyList() }

}
