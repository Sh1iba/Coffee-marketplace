package com.example.coffeeshop

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.coffeeshop.data.managers.PrefsManager
import com.example.coffeeshop.navigation.AppNavGraph
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.screens.BottomMenu
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private val BOTTOM_BAR_ROUTES = setOf(
    NavigationRoutes.HOME,
    NavigationRoutes.FAVORITE,
    NavigationRoutes.CART,
    NavigationRoutes.MY_ORDERS,
    NavigationRoutes.SETTINGS
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefsManager: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination = when {
            prefsManager.isFirstLaunch() -> {
                prefsManager.setFirstLaunchCompleted()
                NavigationRoutes.ONBOARDING
            }
            prefsManager.isLoggedIn() && prefsManager.getRole() == "ADMIN" -> NavigationRoutes.ADMIN_DASHBOARD
            prefsManager.isLoggedIn() -> NavigationRoutes.HOME
            else -> NavigationRoutes.SIGN_IN
        }

        setContent {
            var darkTheme by remember {
                mutableStateOf(prefsManager.getBoolean(PrefsManager.KEY_DARK_MODE, false))
            }

            CoffeeShopTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    bottomBar = {
                        if (currentRoute in BOTTOM_BAR_ROUTES) {
                            BottomMenu(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController     = navController,
                        startDestination  = startDestination,
                        darkThemeEnabled  = darkTheme,
                        onThemeChanged    = { newTheme ->
                            darkTheme = newTheme
                            prefsManager.saveBoolean(PrefsManager.KEY_DARK_MODE, newTheme)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
