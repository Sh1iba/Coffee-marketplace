package com.example.coffeeshop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.coffeeshop.data.remote.response.CartItemResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.ProductVariantResponse
import com.example.coffeeshop.presentation.screens.*
import com.example.coffeeshop.presentation.screens.favorite.FavoriteCoffeeScreen
import com.example.coffeeshop.presentation.ui.screens.OnboardingScreen
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URLDecoder

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    darkThemeEnabled: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(NavigationRoutes.ONBOARDING) {
            OnboardingScreen(navController)
        }

        composable(NavigationRoutes.REGISTRATION) {
            RegistrationScreen(navController)
        }

        composable(NavigationRoutes.SIGN_IN) {
            SignInScreen(navController)
        }

        composable(NavigationRoutes.HOME) {
            HomeScreen(navController)
        }

        composable(NavigationRoutes.FAVORITE) {
            FavoriteCoffeeScreen(navController)
        }

        composable(NavigationRoutes.CART) {
            CartScreen(navController)
        }

        // ── Product detail ──────────────────────────────────────────────────
        composable(
            route = "${NavigationRoutes.DETAIL}/{coffeeId}/{coffeeName}/{coffeeType}/{coffeeDescription}/{imageName}" +
                    "?sizes={sizes}&favoriteSize={favoriteSize}&sellerId={sellerId}",
            arguments = listOf(
                navArgument("coffeeId")          { type = NavType.IntType },
                navArgument("coffeeName")        { type = NavType.StringType },
                navArgument("coffeeType")        { type = NavType.StringType },
                navArgument("coffeeDescription") { type = NavType.StringType },
                navArgument("imageName")         { type = NavType.StringType },
                navArgument("sizes")             { type = NavType.StringType; defaultValue = "" },
                navArgument("favoriteSize")      { type = NavType.StringType; defaultValue = "" },
                navArgument("sellerId")          { type = NavType.LongType;   defaultValue = -1L }
            )
        ) { entry ->
            val sizesEncoded = entry.arguments?.getString("sizes") ?: ""
            val sizes = if (sizesEncoded.isEmpty()) emptyList() else try {
                URLDecoder.decode(sizesEncoded, "UTF-8").split(",").mapNotNull { sp ->
                    val p = sp.split(":")
                    if (p.size >= 2) ProductVariantResponse(
                        size = p[0],
                        price = p[1].toFloatOrNull() ?: 0f,
                        volume = p.getOrNull(2)?.takeIf { it.isNotBlank() }
                    ) else null
                }
            } catch (_: Exception) { emptyList() }

            val sellerId = entry.arguments?.getLong("sellerId") ?: -1L
            CoffeeDetailScreen(
                navController = navController,
                coffee = ProductResponse(
                    id          = entry.arguments?.getInt("coffeeId") ?: 0,
                    type        = ProductCategoryResponse(0, entry.arguments?.getString("coffeeType") ?: ""),
                    name        = entry.arguments?.getString("coffeeName") ?: "",
                    description = entry.arguments?.getString("coffeeDescription") ?: "",
                    sizes       = sizes,
                    imageName   = entry.arguments?.getString("imageName") ?: "",
                    sellerId    = sellerId.takeIf { it > 0 }
                ),
                favoriteSize = entry.arguments?.getString("favoriteSize") ?: ""
            )
        }

        // ── Order checkout ──────────────────────────────────────────────────
        composable(
            route = "${NavigationRoutes.ORDER}?selectedItems={selectedItems}&totalPrice={totalPrice}",
            arguments = listOf(
                navArgument("selectedItems") { type = NavType.StringType; defaultValue = "" },
                navArgument("totalPrice")    { type = NavType.FloatType;  defaultValue = 0f }
            )
        ) { entry ->
            val json       = entry.arguments?.getString("selectedItems") ?: ""
            val totalPrice = entry.arguments?.getFloat("totalPrice") ?: 0f
            val items: List<CartItemResponse> = if (json.isEmpty()) emptyList() else try {
                val decoded = URLDecoder.decode(json, "UTF-8")
                Gson().fromJson(decoded, object : TypeToken<List<CartItemResponse>>() {}.type) ?: emptyList()
            } catch (_: Exception) { emptyList() }

            OrderScreen(navController = navController, selectedItems = items, totalPrice = totalPrice.toDouble())
        }

        // ── My active orders (bottom bar tab) ──────────────────────────────
        composable(NavigationRoutes.MY_ORDERS) {
            MyOrdersScreen(navController)
        }

        // ── Order history (from Settings) ───────────────────────────────────
        composable(NavigationRoutes.ORDER_HISTORY) {
            OrderHistoryScreen(navController)
        }

        // ── Settings ────────────────────────────────────────────────────────
        composable(NavigationRoutes.SETTINGS) {
            SettingsScreen(
                navController   = navController,
                onThemeChanged  = { newTheme ->
                    onThemeChanged(newTheme)
                }
            )
        }

        // ── Seller ──────────────────────────────────────────────────────────
        composable(NavigationRoutes.BECOME_SELLER) {
            BecomeSellerScreen(navController)
        }

        composable(NavigationRoutes.SELLER_DASHBOARD) {
            SellerDashboardScreen(navController)
        }

        composable(
            route = "${NavigationRoutes.SELLER_STORE}/{sellerId}",
            arguments = listOf(navArgument("sellerId") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("sellerId") ?: return@composable
            SellerStoreScreen(navController = navController, sellerId = id)
        }

        // ── Legacy routes (kept for back-compat) ───────────────────────────
        composable(
            route = "${NavigationRoutes.ACTIVE_ORDER}/{orderId}",
            arguments = listOf(navArgument("orderId") { type = NavType.LongType; defaultValue = 0L })
        ) {
            ActiveOrderScreen(navController)
        }

        composable(NavigationRoutes.PICKUP_READY_ORDER) {
            PickupReadyScreen(navController)
        }
    }
}
