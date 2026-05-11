package com.example.coffeeshop.data.remote.api

import com.example.coffeeshop.data.remote.response.AdminCourierResponse
import com.example.coffeeshop.data.remote.response.AdminUserResponse
import com.example.coffeeshop.data.remote.response.ApiResponse
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.data.remote.response.CartSummaryResponse
import com.example.coffeeshop.data.remote.response.CreateOrderResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.FavoriteProductResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.data.remote.response.LoginResponse
import com.example.coffeeshop.data.remote.response.OrderResponse
import com.example.coffeeshop.data.remote.response.PagedProductResponse
import com.example.coffeeshop.data.remote.response.RegisterResponse
import com.example.coffeeshop.data.remote.response.SellerOrderResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.domain.BranchRequest
import com.example.coffeeshop.domain.CartItemRequest
import com.example.coffeeshop.domain.RejectSellerRequest
import com.example.coffeeshop.domain.RoleChangeRequest
import com.example.coffeeshop.domain.FavoriteProductRequest
import com.example.coffeeshop.domain.LoginRequest
import com.example.coffeeshop.domain.OrderRequest
import com.example.coffeeshop.domain.OrderStatusRequest
import com.example.coffeeshop.domain.ProductManageRequest
import com.example.coffeeshop.domain.RegisterRequest
import com.example.coffeeshop.domain.SellerRequest
import com.example.coffeeshop.domain.UpdateCartQuantityRequest
import com.example.coffeeshop.domain.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ── AUTH ───────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    // ── КАТАЛОГ ────────────────────────────────────────────────────────────

    @GET("products/categories")
    suspend fun getAllCoffeeTypes(): Response<List<ProductCategoryResponse>>

    @GET("products")
    suspend fun getAllCoffee(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100
    ): Response<PagedProductResponse>

    // ── ИЗБРАННОЕ ──────────────────────────────────────────────────────────

    @GET("products/favorites")
    suspend fun getFavorites(): Response<List<FavoriteProductResponse>>

    @POST("products/favorites")
    suspend fun addToFavorites(@Body request: FavoriteProductRequest): Response<ApiResponse>

    @DELETE("products/favorites/{productId}")
    suspend fun removeFromFavorites(
        @Path("productId") coffeeId: Int,
        @Query("size") size: String? = null
    ): Response<ApiResponse>

    // ── КОРЗИНА ────────────────────────────────────────────────────────────

    @GET("products/cart")
    suspend fun getCart(): Response<CartSummaryResponse>

    @POST("products/cart")
    suspend fun addToCart(@Body request: CartItemRequest): Response<ApiResponse>

    @PUT("products/cart/{productId}/{selectedSize}")
    suspend fun updateCartQuantity(
        @Path("productId") coffeeId: Int,
        @Path("selectedSize") selectedSize: String,
        @Body request: UpdateCartQuantityRequest
    ): Response<ApiResponse>

    @DELETE("products/cart/{productId}/{selectedSize}")
    suspend fun removeFromCart(
        @Path("productId") coffeeId: Int,
        @Path("selectedSize") selectedSize: String
    ): Response<ApiResponse>

    @DELETE("products/cart")
    suspend fun clearCart(): Response<ApiResponse>

    // ── ЗАКАЗЫ ───────────────────────────────────────────────────────────── 
    @POST("products/checkout")
    suspend fun createOrder(@Body request: OrderRequest): Response<CreateOrderResponse>

    @GET("products/orders/history")
    suspend fun getOrderHistory(): Response<List<OrderResponse>>

    @GET("products/orders/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Long): Response<OrderResponse>

    @PUT("products/orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: Long): Response<ApiResponse>

    // ── РЕКОМЕНДАЦИИ / ПОПУЛЯРНОЕ ─────────────────────────────────────────

    @GET("products/popular")
    suspend fun getPopularProducts(@Query("limit") limit: Int = 8): Response<List<ProductResponse>>

    @GET("products/recommended")
    suspend fun getRecommendedProducts(@Query("limit") limit: Int = 8): Response<List<ProductResponse>>

    @POST("products/{productId}/view")
    suspend fun logProductView(@Path("productId") productId: Int): Response<Unit>

    // ── SELLERS ────────────────────────────────────────────────────────────

    @GET("sellers")
    suspend fun getAllSellers(): Response<List<SellerResponse>>

    @GET("sellers/{id}")
    suspend fun getSellerById(@Path("id") id: Long): Response<SellerResponse>

    @GET("sellers/me")
    suspend fun getMyShop(): Response<SellerResponse>

    @POST("sellers/become-seller")
    suspend fun becomeSeller(@Body request: SellerRequest): Response<SellerResponse>

    @POST("sellers")
    suspend fun createShop(@Body request: SellerRequest): Response<SellerResponse>

    @PUT("sellers/me")
    suspend fun updateMyShop(@Body request: SellerRequest): Response<SellerResponse>

    @PUT("sellers/me/resubmit")
    suspend fun resubmitShop(@Body request: SellerRequest): Response<SellerResponse>

    @GET("sellers/me/products")
    suspend fun getMyProducts(): Response<List<ProductResponse>>

    @POST("sellers/me/products")
    suspend fun createProduct(@Body request: ProductManageRequest): Response<ProductResponse>

    @PUT("sellers/me/products/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: Int,
        @Body request: ProductManageRequest
    ): Response<ProductResponse>

    @DELETE("sellers/me/products/{productId}")
    suspend fun deleteProduct(@Path("productId") productId: Int): Response<ApiResponse>

    @GET("sellers/me/orders")
    suspend fun getMySellerOrders(): Response<List<SellerOrderResponse>>

    @PUT("sellers/me/orders/{orderId}/status")
    suspend fun updateSellerOrderStatus(
        @Path("orderId") orderId: Long,
        @Body request: OrderStatusRequest
    ): Response<ApiResponse>

    @Multipart
    @POST("sellers/me/upload-image")
    suspend fun uploadProductImage(@Part file: MultipartBody.Part): Response<Map<String, String>>

    // ── BRANCHES ───────────────────────────────────────────────────────────

    @GET("sellers/{id}/branches")
    suspend fun getBranchesBySeller(@Path("id") sellerId: Long): Response<List<BranchResponse>>

    @GET("sellers/me/branches")
    suspend fun getMyBranches(): Response<List<BranchResponse>>

    @POST("sellers/me/branches")
    suspend fun createBranch(@Body request: BranchRequest): Response<BranchResponse>

    @PUT("sellers/me/branches/{branchId}")
    suspend fun updateBranch(@Path("branchId") branchId: Long, @Body request: BranchRequest): Response<BranchResponse>

    @PUT("sellers/me/branches/{branchId}/toggle")
    suspend fun toggleBranch(@Path("branchId") branchId: Long): Response<BranchResponse>

    // ── PROFILE ────────────────────────────────────────────────────────────

    @GET("profile")
    suspend fun getProfile(): Response<LoginResponse>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<LoginResponse>

    // ── ADMIN ──────────────────────────────────────────────────────────────

    @GET("admin/users")
    suspend fun getAdminUsers(): Response<List<AdminUserResponse>>

    @PUT("admin/users/{userId}/role")
    suspend fun changeUserRole(
        @Path("userId") userId: Long,
        @Body request: RoleChangeRequest
    ): Response<ApiResponse>

    @GET("admin/sellers")
    suspend fun getAdminAllSellers(): Response<List<SellerResponse>>

    @GET("admin/sellers/pending")
    suspend fun getAdminPendingSellers(): Response<List<SellerResponse>>

    @PUT("admin/sellers/{sellerId}/approve")
    suspend fun approveSeller(@Path("sellerId") sellerId: Long): Response<ApiResponse>

    @PUT("admin/sellers/{sellerId}/reject")
    suspend fun rejectSeller(
        @Path("sellerId") sellerId: Long,
        @Body request: RejectSellerRequest
    ): Response<ApiResponse>

    @PUT("admin/sellers/{sellerId}/activate")
    suspend fun activateSeller(@Path("sellerId") sellerId: Long): Response<ApiResponse>

    @PUT("admin/sellers/{sellerId}/deactivate")
    suspend fun deactivateSeller(@Path("sellerId") sellerId: Long): Response<ApiResponse>

    @GET("admin/couriers")
    suspend fun getAdminCouriers(): Response<List<AdminCourierResponse>>

    @PUT("admin/couriers/{courierId}/toggle")
    suspend fun toggleCourier(@Path("courierId") courierId: Long): Response<ApiResponse>

    @DELETE("admin/couriers/{courierId}")
    suspend fun removeCourier(@Path("courierId") courierId: Long): Response<ApiResponse>
}
