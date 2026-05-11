package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.twotone.ExitToApp
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeeshop.data.remote.response.AdminUserResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.viewmodel.AdminViewModel
import com.example.coffeeshop.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavController) {
    val viewModel: AdminViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val pendingSellers by viewModel.pendingSellers.collectAsState()
    val allSellers by viewModel.allSellers.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.loadPendingSellers()
            1 -> viewModel.loadAllSellers()
            2 -> viewModel.loadAllUsers()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Панель администратора",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            Icons.AutoMirrored.TwoTone.ExitToApp,
                            contentDescription = "Выйти",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = colorDarkOrange
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            if (pendingSellers.isNotEmpty()) "Модерация (${pendingSellers.size})" else "Модерация",
                            fontFamily = SoraFontFamily,
                            fontWeight = if (selectedTab == 0) FontWeight.W600 else FontWeight.W400,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Магазины",
                            fontFamily = SoraFontFamily,
                            fontWeight = if (selectedTab == 1) FontWeight.W600 else FontWeight.W400,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            "Пользователи",
                            fontFamily = SoraFontFamily,
                            fontWeight = if (selectedTab == 2) FontWeight.W600 else FontWeight.W400,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorDarkOrange)
                }
            } else {
                when (selectedTab) {
                    0 -> ModerationTab(
                        sellers = pendingSellers,
                        onApprove = { viewModel.approveSeller(it) },
                        onReject = { id, reason -> viewModel.rejectSeller(id, reason) }
                    )
                    1 -> SellersAdminTab(
                        sellers = allSellers,
                        onToggleActive = { viewModel.toggleSellerActive(it) }
                    )
                    2 -> UsersTab(users = allUsers)
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Выход из аккаунта",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    "Вы уверены, что хотите выйти?",
                    fontFamily = SoraFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.logout()
                    navController.navigate(NavigationRoutes.SIGN_IN) {
                        popUpTo(NavigationRoutes.ADMIN_DASHBOARD) { inclusive = true }
                    }
                    showLogoutDialog = false
                }) {
                    Text(
                        "Выйти",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Отмена", fontFamily = SoraFontFamily, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// ── Модерация ──────────────────────────────────────────────────────────────────

@Composable
private fun ModerationTab(
    sellers: List<SellerResponse>,
    onApprove: (Long) -> Unit,
    onReject: (Long, String) -> Unit
) {
    var rejectTarget by remember { mutableStateOf<SellerResponse?>(null) }
    var rejectReason by remember { mutableStateOf("") }

    if (sellers.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.TwoTone.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Нет заявок на модерацию",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Все магазины проверены",
                    fontFamily = SoraFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sellers, key = { it.id }) { seller ->
                PendingSellerCard(
                    seller = seller,
                    onApprove = { onApprove(seller.id) },
                    onReject = { rejectTarget = seller }
                )
            }
        }
    }

    rejectTarget?.let { seller ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null; rejectReason = "" },
            title = {
                Text(
                    "Отклонить «${seller.name}»",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Укажите причину отклонения:",
                        fontFamily = SoraFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("Например: недостаточно информации о магазине", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onReject(seller.id, rejectReason.trim())
                        rejectTarget = null
                        rejectReason = ""
                    },
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text(
                        "Отклонить",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { rejectTarget = null; rejectReason = "" }) {
                    Text("Отмена", fontFamily = SoraFontFamily, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingSellerCard(
    seller: SellerResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            // Баннер магазина — всегда показываем область
            if (!seller.logoImage.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(seller.logoImage)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Баннер ${seller.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.AccountBox,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Text(
                            "Баннер не добавлен",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Название + статус
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            seller.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            seller.category,
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = colorDarkOrange
                        )
                    }
                    StatusBadge("PENDING")
                }

                // Описание полностью
                Text(
                    seller.description,
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                // Контакты
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdminInfoRow(Icons.TwoTone.AccountCircle, seller.ownerName)
                    if (!seller.phone.isNullOrBlank()) {
                        AdminInfoRow(Icons.TwoTone.Phone, seller.phone)
                    }
                    if (!seller.website.isNullOrBlank()) {
                        AdminInfoRow(Icons.TwoTone.Info, seller.website)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                // Кнопки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Отклонить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp)
                    }
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
                    ) {
                        Text("Одобрить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text, fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Магазины ──────────────────────────────────────────────────────────────────

@Composable
private fun SellersAdminTab(
    sellers: List<SellerResponse>,
    onToggleActive: (SellerResponse) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(sellers, searchQuery) {
        if (searchQuery.isBlank()) sellers
        else sellers.filter { s ->
            s.name.contains(searchQuery, ignoreCase = true) ||
            s.category.contains(searchQuery, ignoreCase = true) ||
            s.ownerName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Поиск по названию или владельцу"
        )

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (searchQuery.isBlank()) "Магазины не найдены" else "Нет результатов по запросу «$searchQuery»",
                    fontFamily = SoraFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { seller ->
                    SellerAdminCard(seller = seller, onToggleActive = { onToggleActive(seller) })
                }
            }
        }
    }
}

@Composable
private fun SellerAdminCard(
    seller: SellerResponse,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        seller.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    StatusBadge(seller.status)
                }
                Text(seller.category, fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange)
                Text(seller.ownerName, fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (seller.status == "APPROVED") {
                Switch(
                    checked = seller.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorDarkOrange,
                        checkedTrackColor = colorDarkOrange.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }
}

// ── Пользователи ──────────────────────────────────────────────────────────────

@Composable
private fun UsersTab(users: List<AdminUserResponse>) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { u ->
            u.name.contains(searchQuery, ignoreCase = true) ||
            u.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        AdminSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Поиск по имени или email"
        )

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (searchQuery.isBlank()) "Пользователи не найдены" else "Нет результатов по запросу «$searchQuery»",
                    fontFamily = SoraFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { user ->
                    UserAdminCard(user)
                }
            }
        }
    }
}

@Composable
private fun UserAdminCard(user: AdminUserResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    user.name,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    user.email,
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RoleBadge(user.role)
        }
    }
}

// ── Общие компоненты ──────────────────────────────────────────────────────────

@Composable
private fun AdminSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = {
            Text(placeholder, fontFamily = SoraFontFamily, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(Icons.TwoTone.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.TwoTone.Clear, contentDescription = "Очистить", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorDarkOrange,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun StatusBadge(status: String) {
    val (badgeColor, label) = when (status) {
        "APPROVED" -> MaterialTheme.colorScheme.primary to "Одобрен"
        "PENDING" -> Color(0xFFF59E0B) to "На рассмотрении"
        "REJECTED" -> MaterialTheme.colorScheme.error to "Отклонён"
        else -> MaterialTheme.colorScheme.outline to status
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = badgeColor.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 10.sp,
            color = badgeColor
        )
    }
}

@Composable
private fun RoleBadge(role: String) {
    val (badgeColor, label) = when (role) {
        "ADMIN" -> MaterialTheme.colorScheme.error to "Админ"
        "SELLER" -> colorDarkOrange to "Продавец"
        "COURIER" -> MaterialTheme.colorScheme.primary to "Курьер"
        else -> MaterialTheme.colorScheme.onSurfaceVariant to "Покупатель"
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = badgeColor.copy(alpha = 0.12f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 11.sp,
            color = badgeColor
        )
    }
}
