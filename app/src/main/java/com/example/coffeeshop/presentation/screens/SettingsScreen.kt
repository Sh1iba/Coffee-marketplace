package com.example.coffeeshop.presentation.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ArrowForward
import androidx.compose.material.icons.automirrored.twotone.ExitToApp
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.navigation.NavigationRoutes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.R
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.viewmodel.SettingsViewModel
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import com.example.coffeeshop.presentation.theme.colorLightWhite
import com.example.coffeeshop.presentation.theme.colorDarkOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onThemeChanged: (Boolean) -> Unit
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current

    val darkModeEnabled by viewModel.darkModeState.collectAsState()
    val notificationsEnabled by viewModel.notificationsState.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    CoffeeShopTheme(darkTheme = darkModeEnabled) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 68.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                            .height(44.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { navController.navigateUp() },
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.leftarrow),
                                contentDescription = "Back",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                            )
                        }

                        Text(
                            text = "Настройки",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            lineHeight = 30.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Box(
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UserInfoSection(viewModel)

                Divider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )

                SettingsOptionsSection(
                    viewModel = viewModel,
                    navController = navController,
                    context = context,
                    darkModeEnabled = darkModeEnabled,
                    notificationsEnabled = notificationsEnabled,
                    onThemeChanged = { newValue ->
                        viewModel.toggleDarkMode(newValue)
                        onThemeChanged(newValue)
                    },
                    onNotificationsChanged = { newValue ->
                        viewModel.toggleNotifications(newValue)
                    }
                )
            }
        }
    }
}

@Composable
private fun UserInfoSection(viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.TwoTone.AccountCircle,
                contentDescription = "Профиль",
                modifier = Modifier.size(48.dp),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Профиль",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    ),
                )

                val userName by viewModel.userName.collectAsState()
                val userEmail by viewModel.userEmail.collectAsState()

                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                        lineHeight = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = userEmail,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        lineHeight = 14.4.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SettingsOptionsSection(
    viewModel: SettingsViewModel,
    navController: NavController,
    context: Context,
    darkModeEnabled: Boolean,
    notificationsEnabled: Boolean,
    onThemeChanged: (Boolean) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isSeller by viewModel.isSeller.collectAsState()
    val shopStatus by viewModel.shopStatus.collectAsState()

    // Уведомления
    SettingsItem(
        title = "Уведомления",
        subtitle = "Вкл/выкл уведомления",
        leadingIcon = Icons.TwoTone.Notifications,
        trailingContent = {
            Switch(
                checked = notificationsEnabled,
                onCheckedChange = { newValue ->
                    onNotificationsChanged(newValue)
                },
                modifier = Modifier.widthIn(max = 52.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorDarkOrange,
                    checkedTrackColor = colorDarkOrange.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        }
    )

    // Темная тема
    SettingsItem(
        title = "Темная тема",
        subtitle = "Включить темный режим",
        leadingIcon = Icons.TwoTone.Build,
        trailingContent = {
            Switch(
                checked = darkModeEnabled,
                onCheckedChange = { newValue ->
                    onThemeChanged(newValue) // Вызываем функцию обновления темы
                },
                modifier = Modifier.widthIn(max = 52.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorDarkOrange,
                    checkedTrackColor = colorDarkOrange.copy(alpha = 0.3f),
                    uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )
        }
    )

    when {
        isSeller && shopStatus == "APPROVED" -> SettingsItem(
            title = "Панель продавца",
            subtitle = "Управление магазином и товарами",
            leadingIcon = Icons.TwoTone.ShoppingCart,
            onClick = { navController.navigate(NavigationRoutes.SELLER_DASHBOARD) }
        )
        isSeller && shopStatus == "PENDING" -> SettingsItem(
            title = "Стать продавцом",
            subtitle = "Заявка на рассмотрении...",
            leadingIcon = Icons.TwoTone.ShoppingCart,
            onClick = { navController.navigate(NavigationRoutes.SELLER_DASHBOARD) }
        )
        isSeller && shopStatus == "REJECTED" -> SettingsItem(
            title = "Стать продавцом",
            subtitle = "Заявка отклонена — нажмите чтобы подать заново",
            leadingIcon = Icons.TwoTone.ShoppingCart,
            onClick = { navController.navigate(NavigationRoutes.SELLER_DASHBOARD) }
        )
        else -> SettingsItem(
            title = "Стать продавцом",
            subtitle = "Откройте свой магазин на платформе",
            leadingIcon = Icons.TwoTone.ShoppingCart,
            onClick = { navController.navigate(NavigationRoutes.BECOME_SELLER) }
        )
    }

    SettingsItem(
        title = "История заказов",
        subtitle = "Просмотр ваших заказов",
        leadingIcon = Icons.TwoTone.DateRange,
        onClick = {
            navController.navigate("order_history")
        }
    )

    // Связаться с нами
    SettingsItem(
        title = "Связаться с нами",
        subtitle = "Поддержка и обратная связь",
        leadingIcon = Icons.TwoTone.Email,
        onClick = {
            openTelegram(context, "https://t.me/b0neless0")
        }
    )

    // О приложении
    SettingsItem(
        title = "О приложении",
        subtitle = "Версия 1.0.0",
        leadingIcon = Icons.TwoTone.Info,
        onClick = {
            // TODO: Реализовать экран информации о приложении
        }
    )

    // Разделитель перед выходом
    Divider(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )

    SettingsItem(
        title = "Выйти из аккаунта",
        subtitle = "Завершить текущую сессию",
        leadingIcon = Icons.AutoMirrored.TwoTone.ExitToApp,
        trailingContent = null,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.error,
        onClick = {
            showLogoutDialog = true
        }
    )

    // Диалог подтверждения выхода
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Выход из аккаунта",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 20.sp,
                    lineHeight = 30.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите выйти из аккаунта?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.logout()
                        navController.navigate(NavigationRoutes.SIGN_IN) {
                            popUpTo(NavigationRoutes.HOME) {
                                inclusive = true
                            }
                        }
                        showLogoutDialog = false
                    }
                ) {
                    Text(
                        "Выйти",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text(
                        "Отмена",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        shape = MaterialTheme.shapes.large,
        onClick = { onClick?.invoke() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            lineHeight = 24.sp
                        ),
                        color = contentColor
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W400,
                                fontSize = 12.sp,
                                lineHeight = 14.4.sp
                            ),
                            color = if (contentColor == MaterialTheme.colorScheme.error) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                contentColor.copy(alpha = 0.6f)
                            }
                        )
                    }
                }
            }

            trailingContent?.invoke() ?: run {
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.AutoMirrored.TwoTone.ArrowForward,
                        contentDescription = "Перейти",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

fun openTelegram(context: Context, telegramUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
        context.startActivity(webIntent)
    }
}