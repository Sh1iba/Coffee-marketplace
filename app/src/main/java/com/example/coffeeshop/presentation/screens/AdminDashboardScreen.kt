package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.coffeeshop.data.remote.response.ProductResponse
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
    val pendingProducts by viewModel.pendingProducts.collectAsState()
    val allSellers by viewModel.allSellers.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val sellerProducts by viewModel.sellerProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> { viewModel.loadPendingSellers(); viewModel.loadPendingProducts() }
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
                        val total = pendingSellers.size + pendingProducts.size
                        Text(
                            if (total > 0) "Модерация ($total)" else "Модерация",
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
                        pendingProducts = pendingProducts,
                        sellerProducts = sellerProducts,
                        onApprove = { viewModel.approveSeller(it) },
                        onReject = { id, reason -> viewModel.rejectSeller(id, reason) },
                        onLoadProducts = { viewModel.loadSellerProducts(it) },
                        onApproveProduct = { sid, pid -> viewModel.approveProduct(sid, pid) },
                        onRejectProduct = { sid, pid, reason -> viewModel.rejectProduct(sid, pid, reason) },
                        onDeleteProduct = { sid, pid -> viewModel.deleteProduct(sid, pid) }
                    )
                    1 -> SellersAdminTab(
                        sellers = allSellers,
                        sellerProducts = sellerProducts,
                        onToggleActive = { viewModel.toggleSellerActive(it) },
                        onLoadProducts = { viewModel.loadSellerProducts(it) },
                        onApproveProduct = { sid, pid -> viewModel.approveProduct(sid, pid) },
                        onRejectProduct = { sid, pid, reason -> viewModel.rejectProduct(sid, pid, reason) },
                        onDeleteProduct = { sid, pid -> viewModel.deleteProduct(sid, pid) }
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
    pendingProducts: List<ProductResponse>,
    sellerProducts: Map<Long, List<ProductResponse>>,
    onApprove: (Long) -> Unit,
    onReject: (Long, String) -> Unit,
    onLoadProducts: (Long) -> Unit,
    onApproveProduct: (Long, Int) -> Unit,
    onRejectProduct: (Long, Int, String) -> Unit,
    onDeleteProduct: (Long, Int) -> Unit
) {
    var rejectTarget by remember { mutableStateOf<SellerResponse?>(null) }
    var rejectReason by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) } // 0=Все, 1=Магазины, 2=Товары

    val showSellers = filter == 0 || filter == 1
    val showProducts = filter == 0 || filter == 2

    Column(modifier = Modifier.fillMaxSize()) {
        // ── Фильтр-чипы ────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Все ${sellers.size + pendingProducts.size}",
                "Магазины ${sellers.size}",
                "Товары ${pendingProducts.size}"
            ).forEachIndexed { idx, label ->
                FilterChip(
                    selected = filter == idx,
                    onClick = { filter = idx },
                    label = {
                        Text(
                            label,
                            fontFamily = SoraFontFamily,
                            fontWeight = if (filter == idx) FontWeight.W600 else FontWeight.W400,
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = colorDarkOrange,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (sellers.isEmpty() && pendingProducts.isEmpty()) {
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
                        "Всё проверено",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Нет новых заявок и товаров",
                        fontFamily = SoraFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ── Товары на проверке ──────────────────────────────────────
                if (showProducts && pendingProducts.isNotEmpty()) {
                    item {
                        Text(
                            "Товары на проверке (${pendingProducts.size})",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W700,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(pendingProducts, key = { "product_${it.id}" }) { product ->
                        val sellerId = product.sellerId ?: -1L
                        ProductModerationCard(
                            product = product,
                            onApprove = { onApproveProduct(sellerId, product.id) },
                            onReject = { },
                            onDelete = { onDeleteProduct(sellerId, product.id) },
                            onRejectWithReason = { reason -> onRejectProduct(sellerId, product.id, reason) }
                        )
                    }
                }

                // ── Заявки магазинов ────────────────────────────────────────
                if (showSellers && sellers.isNotEmpty()) {
                    item {
                        Text(
                            "Заявки магазинов (${sellers.size})",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W700,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    items(sellers, key = { it.id }) { seller ->
                        PendingSellerCard(
                            seller = seller,
                            products = sellerProducts[seller.id],
                            onApprove = { onApprove(seller.id) },
                            onReject = { rejectTarget = seller },
                            onLoadProducts = { onLoadProducts(seller.id) },
                            onApproveProduct = { pid -> onApproveProduct(seller.id, pid) },
                            onRejectProduct = { pid, reason -> onRejectProduct(seller.id, pid, reason) },
                            onDeleteProduct = { pid -> onDeleteProduct(seller.id, pid) }
                        )
                    }
                }
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
    products: List<ProductResponse>?,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onLoadProducts: () -> Unit,
    onApproveProduct: (Int) -> Unit,
    onRejectProduct: (Int, String) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    val context = LocalContext.current
    var productsExpanded by remember { mutableStateOf(false) }
    var rejectProductTarget by remember { mutableStateOf<ProductResponse?>(null) }
    var rejectProductReason by remember { mutableStateOf("") }

    LaunchedEffect(productsExpanded) {
        if (productsExpanded && products == null) onLoadProducts()
    }

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

                // Раздел товаров
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { productsExpanded = !productsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.TwoTone.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            if (products != null) "Товары (${products.size})" else "Посмотреть товары",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Icon(
                        if (productsExpanded) Icons.TwoTone.KeyboardArrowUp else Icons.TwoTone.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (productsExpanded) {
                    if (products == null) {
                        Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorDarkOrange, strokeWidth = 2.dp)
                        }
                    } else if (products.isEmpty()) {
                        Text(
                            "Товары ещё не добавлены",
                            fontFamily = SoraFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            products.forEach { product ->
                                ProductModerationCard(
                                    product = product,
                                    onApprove = { onApproveProduct(product.id) },
                                    onReject = { rejectProductTarget = product },
                                    onDelete = { onDeleteProduct(product.id) }
                                )
                            }
                        }
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

    rejectProductTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { rejectProductTarget = null; rejectProductReason = "" },
            title = {
                Text(
                    "Отклонить «${product.name}»",
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
                        value = rejectProductReason,
                        onValueChange = { rejectProductReason = it },
                        placeholder = { Text("Например: фото не соответствует товару", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectProduct(product.id, rejectProductReason.trim())
                        rejectProductTarget = null
                        rejectProductReason = ""
                    },
                    enabled = rejectProductReason.isNotBlank()
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
                TextButton(onClick = { rejectProductTarget = null; rejectProductReason = "" }) {
                    Text("Отмена", fontFamily = SoraFontFamily, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun ProductModerationCard(
    product: ProductResponse,
    onApprove: () -> Unit,
    onReject: () -> Unit = {},
    onDelete: () -> Unit,
    onRejectWithReason: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }

    val borderColor = when (product.status) {
        "APPROVED" -> Color(0xFF22C55E)
        "REJECTED" -> MaterialTheme.colorScheme.error
        else -> Color(0xFFF59E0B)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Верхняя строка: фото + основная инфа + бейдж
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(product.imageUrl).crossfade(true).build(),
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            product.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W700,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        StatusBadge(product.status)
                    }
                    Text(
                        product.type.type,
                        fontFamily = SoraFontFamily,
                        fontSize = 11.sp,
                        color = colorDarkOrange,
                        fontWeight = FontWeight.W600
                    )
                    // Цены по вариантам
                    if (product.sizes.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            product.sizes.forEach { variant ->
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        "${variant.size} — ${variant.price.toInt()}₽",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = SoraFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.W600,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Описание — раскрывается по кнопке
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "Скрыть описание" else "Показать описание",
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    if (expanded) Icons.TwoTone.KeyboardArrowUp else Icons.TwoTone.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (expanded) {
                Text(
                    product.description,
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Причина отклонения
            if (!product.rejectionReason.isNullOrBlank()) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.TwoTone.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp).padding(top = 1.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            product.rejectionReason,
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (product.status != "APPROVED") {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Icon(Icons.TwoTone.CheckCircle, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Одобрить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 12.sp)
                    }
                }
                if (product.status != "REJECTED") {
                    OutlinedButton(
                        onClick = { if (onRejectWithReason != null) showRejectDialog = true else onReject() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.TwoTone.Close, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Отклонить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.TwoTone.Delete,
                        contentDescription = "Удалить",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false; rejectReason = "" },
            title = {
                Text(
                    "Отклонить «${product.name}»",
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
                        placeholder = { Text("Например: фото не соответствует товару", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectWithReason?.invoke(rejectReason.trim())
                        showRejectDialog = false
                        rejectReason = ""
                    },
                    enabled = rejectReason.isNotBlank()
                ) {
                    Text("Отклонить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false; rejectReason = "" }) {
                    Text("Отмена", fontFamily = SoraFontFamily, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
    sellerProducts: Map<Long, List<ProductResponse>>,
    onToggleActive: (SellerResponse) -> Unit,
    onLoadProducts: (Long) -> Unit,
    onApproveProduct: (Long, Int) -> Unit,
    onRejectProduct: (Long, Int, String) -> Unit,
    onDeleteProduct: (Long, Int) -> Unit
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
                    SellerAdminCard(
                        seller = seller,
                        products = sellerProducts[seller.id],
                        onToggleActive = { onToggleActive(seller) },
                        onLoadProducts = { onLoadProducts(seller.id) },
                        onApproveProduct = { pid -> onApproveProduct(seller.id, pid) },
                        onRejectProduct = { pid, reason -> onRejectProduct(seller.id, pid, reason) },
                        onDeleteProduct = { pid -> onDeleteProduct(seller.id, pid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SellerAdminCard(
    seller: SellerResponse,
    products: List<ProductResponse>?,
    onToggleActive: () -> Unit,
    onLoadProducts: () -> Unit,
    onApproveProduct: (Int) -> Unit,
    onRejectProduct: (Int, String) -> Unit,
    onDeleteProduct: (Int) -> Unit
) {
    var productsExpanded by remember { mutableStateOf(false) }
    var rejectProductTarget by remember { mutableStateOf<ProductResponse?>(null) }
    var rejectProductReason by remember { mutableStateOf("") }

    LaunchedEffect(productsExpanded) {
        if (productsExpanded && products == null) onLoadProducts()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { productsExpanded = !productsExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (products != null) "Товары (${products.size})" else "Посмотреть товары",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    if (productsExpanded) Icons.TwoTone.KeyboardArrowUp else Icons.TwoTone.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (productsExpanded) {
                if (products == null) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colorDarkOrange, strokeWidth = 2.dp)
                    }
                } else if (products.isEmpty()) {
                    Text(
                        "Товары ещё не добавлены",
                        fontFamily = SoraFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        products.forEach { product ->
                            ProductModerationCard(
                                product = product,
                                onApprove = { onApproveProduct(product.id) },
                                onReject = { rejectProductTarget = product },
                                onDelete = { onDeleteProduct(product.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    rejectProductTarget?.let { product ->
        AlertDialog(
            onDismissRequest = { rejectProductTarget = null; rejectProductReason = "" },
            title = {
                Text(
                    "Отклонить «${product.name}»",
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
                        value = rejectProductReason,
                        onValueChange = { rejectProductReason = it },
                        placeholder = { Text("Например: фото не соответствует товару", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRejectProduct(product.id, rejectProductReason.trim())
                        rejectProductTarget = null
                        rejectProductReason = ""
                    },
                    enabled = rejectProductReason.isNotBlank()
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
                TextButton(onClick = { rejectProductTarget = null; rejectProductReason = "" }) {
                    Text("Отмена", fontFamily = SoraFontFamily, color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
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
