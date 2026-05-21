package com.example.coffeeshop.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.CameraUpdateReason
import com.yandex.mapkit.map.Map as YMap
import com.yandex.mapkit.mapview.MapView as YMapView
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.coffeeshop.R
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerOrderResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.domain.BranchRequest
import com.example.coffeeshop.domain.ProductManageRequest
import com.example.coffeeshop.domain.SellerRequest
import com.example.coffeeshop.domain.VariantRequest
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.viewmodel.SellerViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(navController: NavController) {
    val viewModel: SellerViewModel = hiltViewModel()

    val myShop by viewModel.myShop.collectAsState()
    val myProducts by viewModel.myProducts.collectAsState()
    val myBranches by viewModel.myBranches.collectAsState()
    val myReviews by viewModel.myReviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Магазин", "Товары", "Филиалы", "Отзывы")

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> { viewModel.loadMyProducts(); viewModel.loadMyBranches() }
            1 -> viewModel.loadMyProducts()
            2 -> viewModel.loadMyBranches()
            3 -> viewModel.loadMyReviews()
        }
    }

    // Только REJECTED показывает отдельный экран — PENDING теперь показывает полный дашборд
    if (!isLoading && myShop?.status == "REJECTED") {
        ModerationRejectedContent(
            navController = navController,
            shop = myShop!!,
            isUploading = isUploading,
            onUploadImage = { part, cb -> viewModel.uploadImage(part, cb) },
            onResubmit = { req -> viewModel.resubmitShop(req) { viewModel.loadMyShop() } },
            onRefresh = { viewModel.loadMyShop() }
        )
        return
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)
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
                            contentDescription = "Назад",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                        )
                    }
                    Text(
                        text = "Панель продавца",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Box(modifier = Modifier.size(44.dp))
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = colorDarkOrange
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = if (selectedTab == index) FontWeight.W600 else FontWeight.W400,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = colorDarkOrange
                )
            } else {
                when (selectedTab) {
                    0 -> ShopTab(
                        myShop, viewModel,
                        approvedProductCount = myProducts.count { it.status == "APPROVED" },
                        pendingProductCount = myProducts.count { it.status == "PENDING" },
                        approvedBranchCount = myBranches.count { it.status == "APPROVED" },
                        pendingBranchCount = myBranches.count { it.status == "PENDING" }
                    )
                    1 -> ProductsTab(myProducts, viewModel)
                    2 -> BranchesTab(myBranches, viewModel)
                    3 -> SellerReviewsTab(reviews = myReviews, rating = myShop?.rating ?: 0.0)
                }
            }

            error?.let { msg ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK", color = colorDarkOrange)
                        }
                    }
                ) { Text(msg) }
            }
        }
    }
}

@Composable
private fun ShopTab(shop: SellerResponse?, viewModel: SellerViewModel, approvedProductCount: Int, pendingProductCount: Int, approvedBranchCount: Int, pendingBranchCount: Int) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val isUploading by viewModel.isUploading.collectAsState()

    if (shop == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.TwoTone.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "У вас ещё нет магазина",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Создайте магазин, чтобы начать продавать кофе",
                    fontFamily = SoraFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Создать магазин", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                when (shop.status) {
                    "PENDING" -> ModerationBanner(
                        color = Color(0xFFFFF8E1),
                        borderColor = Color(0xFFFFB300),
                        icon = Icons.TwoTone.Info,
                        iconTint = Color(0xFFFFB300),
                        title = "На модерации",
                        subtitle = "Ваш магазин проверяется. Обычно это занимает до 24 часов."
                    )
                    "REJECTED" -> ModerationBanner(
                        color = MaterialTheme.colorScheme.errorContainer,
                        borderColor = MaterialTheme.colorScheme.error,
                        icon = Icons.TwoTone.Info,
                        iconTint = MaterialTheme.colorScheme.error,
                        title = "Заявка отклонена",
                        subtitle = shop.rejectionReason ?: "Причина не указана"
                    )
                    else -> Unit
                }
            }
            if (shop.status == "APPROVED" || shop.status == "PENDING") {
                item {
                    ReadinessBanner(
                        approvedProductCount = approvedProductCount,
                        pendingProductCount = pendingProductCount,
                        approvedBranchCount = approvedBranchCount,
                        pendingBranchCount = pendingBranchCount,
                        isPending = shop.status == "PENDING"
                    )
                }
            }
            item {
                ShopInfoCard(shop, onEdit = { showEditDialog = true })
            }
            item {
                ShopStatsCard(shop)
            }
        }
    }

    if (showCreateDialog) {
        ShopFormDialog(
            title = "Создать магазин",
            initial = null,
            isUploading = isUploading,
            onUploadImage = { part, cb -> viewModel.uploadImage(part, cb) },
            onConfirm = { req ->
                viewModel.createShop(req) {}
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }

    if (showEditDialog && shop != null) {
        ShopFormDialog(
            title = "Редактировать магазин",
            initial = shop,
            isUploading = isUploading,
            onUploadImage = { part, cb -> viewModel.uploadImage(part, cb) },
            onConfirm = { req ->
                viewModel.updateShop(req) {}
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
private fun ModerationBanner(
    color: Color,
    borderColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = iconTint)
                Text(subtitle, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ReadinessBanner(approvedProductCount: Int, pendingProductCount: Int, approvedBranchCount: Int, pendingBranchCount: Int, isPending: Boolean = false) {
    val hasBranch = approvedBranchCount >= 1
    val hasProducts = approvedProductCount >= 5
    val isReady = hasBranch && hasProducts && !isPending

    if (isReady) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE8F5E9),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.TwoTone.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                Text(
                    "Магазин виден покупателям в каталоге",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    } else {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colorDarkOrange.copy(alpha = 0.07f),
            border = androidx.compose.foundation.BorderStroke(1.dp, colorDarkOrange.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.TwoTone.Info, null, tint = colorDarkOrange, modifier = Modifier.size(18.dp))
                    Text(
                        if (isPending) "Подготовьте магазин пока идёт проверка"
                        else "Магазин пока не виден покупателям",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 14.sp,
                        color = colorDarkOrange
                    )
                }
                Text(
                    if (isPending)
                        "Выполните шаги заранее — после одобрения магазин появится в каталоге сразу:"
                    else
                        "Выполните все шаги чтобы ваш магазин появился в каталоге:",
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                ReadinessStep(
                    done = !isPending,
                    text = if (isPending) "Дождитесь одобрения модератором" else "Магазин одобрен модератором"
                )
                ReadinessStep(
                    done = hasBranch,
                    pending = !hasBranch && pendingBranchCount > 0,
                    text = when {
                        hasBranch -> "Филиал одобрен ($approvedBranchCount)"
                        pendingBranchCount > 0 -> "Филиал на проверке — ожидайте одобрения"
                        else -> "Добавьте хотя бы 1 филиал"
                    }
                )
                ReadinessStep(
                    done = hasProducts,
                    pending = !hasProducts && pendingProductCount > 0,
                    text = when {
                        hasProducts -> "Товары одобрены ($approvedProductCount/5)"
                        pendingProductCount > 0 -> "На проверке $pendingProductCount товар(а) — одобрено $approvedProductCount/5"
                        else -> "Добавьте минимум 5 товаров ($approvedProductCount/5)"
                    }
                )
            }
        }
    }
}

@Composable
private fun ReadinessStep(done: Boolean, pending: Boolean = false, text: String) {
    val iconColor = when {
        done -> Color(0xFF4CAF50)
        pending -> Color(0xFFF59E0B)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val icon = when {
        done -> Icons.TwoTone.CheckCircle
        pending -> Icons.Filled.AccessTime
        else -> Icons.TwoTone.Info
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = iconColor
        )
        Text(
            text,
            fontFamily = SoraFontFamily,
            fontSize = 13.sp,
            fontWeight = if (done) FontWeight.W400 else FontWeight.W600,
            color = if (pending) Color(0xFFF59E0B) else if (done) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SellerReviewsTab(reviews: List<com.example.coffeeshop.data.remote.response.ReviewResponse>, rating: Double) {
    if (reviews.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.TwoTone.Star, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Отзывов пока нет", fontFamily = SoraFontFamily, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val average = reviews.map { it.rating }.average()
    val distribution = (5 downTo 1).map { star -> star to reviews.count { it.rating == star } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "%.1f".format(average),
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W700,
                            fontSize = 40.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            repeat(5) { i ->
                                Icon(
                                    Icons.TwoTone.Star, null,
                                    tint = if (i < average.toInt()) colorDarkOrange else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text("${reviews.size} отзывов", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        distribution.forEach { (star, count) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("$star", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(10.dp))
                                Icon(Icons.TwoTone.Star, null, tint = colorDarkOrange, modifier = Modifier.size(12.dp))
                                LinearProgressIndicator(
                                    progress = { if (reviews.isEmpty()) 0f else count.toFloat() / reviews.size },
                                    modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = colorDarkOrange,
                                    trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                )
                                Text("$count", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(20.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }
        }
        items(reviews) { review ->
            ReviewCard(review = review)
        }
    }
}

@Composable
private fun ShopInfoCard(shop: SellerResponse, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        if (!shop.logoImage.isNullOrBlank()) {
            AsyncImage(
                model = shop.logoImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        shop.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        shop.category,
                        fontFamily = SoraFontFamily,
                        fontSize = 13.sp,
                        color = colorDarkOrange
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.TwoTone.Create, "Редактировать", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(
                shop.description,
                fontFamily = SoraFontFamily,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.TwoTone.Star, null, tint = colorDarkOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    "%.1f".format(shop.rating),
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.width(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (shop.isActive) colorDarkOrange.copy(alpha = 0.15f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        if (shop.isActive) "Активен" else "Неактивен",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 12.sp,
                        color = if (shop.isActive) colorDarkOrange else MaterialTheme.colorScheme.error
                    )
                }
            }
            if (!shop.phone.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.TwoTone.Phone, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(shop.phone, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!shop.website.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.TwoTone.Share, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(shop.website, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun ShopStatsCard(shop: SellerResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem(icon = Icons.TwoTone.Person, label = "Владелец", value = shop.ownerName)
            StatItem(icon = Icons.TwoTone.Star, label = "Рейтинг", value = "%.1f".format(shop.rating))
        }
    }
}

@Composable
private fun StatItem(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = colorDarkOrange, modifier = Modifier.size(24.dp))
        Text(value, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(label, fontFamily = SoraFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsTab(products: List<ProductResponse>, viewModel: SellerViewModel) {
    val categories by viewModel.categories.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductResponse?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) { viewModel.loadCategories() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Мои товары (${products.size})",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Добавить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp)
            }
        }

        if (products.isNotEmpty()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Поиск товара...", fontFamily = SoraFontFamily, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.TwoTone.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.TwoTone.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDarkOrange,
                    focusedLabelColor = colorDarkOrange
                )
            )
        }

        if (products.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.TwoTone.Place, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Товары ещё не добавлены", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Ничего не найдено", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 0.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredProducts) { product ->
                    ProductItemCard(
                        product = product,
                        onEdit = { editingProduct = product },
                        onDelete = { viewModel.deleteProduct(product.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ProductFormDialog(
            title = "Добавить товар",
            initial = null,
            categories = categories,
            isUploading = isUploading,
            onUploadImage = { part, cb -> viewModel.uploadImage(part, cb) },
            onConfirm = { req -> viewModel.createProduct(req) {}; showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    editingProduct?.let { product ->
        ProductFormDialog(
            title = "Редактировать товар",
            initial = product,
            categories = categories,
            isUploading = isUploading,
            onUploadImage = { part, cb -> viewModel.uploadImage(part, cb) },
            onConfirm = { req -> viewModel.updateProduct(product.id, req) {}; editingProduct = null },
            onDismiss = { editingProduct = null }
        )
    }
}

@Composable
private fun ProductItemCard(
    product: ProductResponse,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        product.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    ProductStatusBadge(product.status)
                }
                Text(product.type.type, fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange)
                val priceText = product.sizes.joinToString(" · ") { "${it.size}: ${it.price.toInt()}₽" }
                Text(priceText, fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!product.rejectionReason.isNullOrBlank()) {
                    Text(
                        "Причина: ${product.rejectionReason}",
                        fontFamily = SoraFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.TwoTone.Create, "Редактировать", tint = colorDarkOrange)
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.TwoTone.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить товар?", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600) },
            text = { Text("«${product.name}» будет удалён навсегда.", fontFamily = SoraFontFamily) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена", fontFamily = SoraFontFamily)
                }
            }
        )
    }
}

@Composable
private fun ProductStatusBadge(status: String) {
    val (color, label) = when (status) {
        "APPROVED" -> Color(0xFF22C55E) to "Одобрен"
        "PENDING" -> Color(0xFFF59E0B) to "На проверке"
        "REJECTED" -> MaterialTheme.colorScheme.error to "Отклонён"
        else -> MaterialTheme.colorScheme.outline to status
    }
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 10.sp,
            color = color
        )
    }
}

@Composable
private fun OrdersTab(orders: List<SellerOrderResponse>, viewModel: SellerViewModel) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.TwoTone.Create, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Заказов пока нет", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(orders) { order ->
                SellerOrderCard(order, onStatusUpdate = { newStatus ->
                    viewModel.updateOrderStatus(order.orderId, newStatus)
                })
            }
        }
    }
}

private fun nextSellerStatus(current: String): String? = when (current) {
    "PENDING"          -> "CONFIRMED"
    "CONFIRMED"        -> "COOKING"
    "COOKING"          -> "READY_FOR_PICKUP"
    "READY_FOR_PICKUP" -> "PICKED_UP"
    "PICKED_UP"        -> "DELIVERING"
    "DELIVERING"       -> "DELIVERED"
    else               -> null
}

private fun statusLabel(status: String) = when (status) {
    "PENDING"          -> "Ожидает"
    "CONFIRMED"        -> "Подтверждён"
    "COOKING"          -> "Готовится"
    "READY_FOR_PICKUP" -> "Готов к выдаче"
    "PICKED_UP"        -> "Курьер забрал"
    "DELIVERING"       -> "В пути"
    "DELIVERED"        -> "Доставлен"
    "CANCELLED"        -> "Отменён"
    else               -> status
}

private fun statusColor(status: String) = when (status) {
    "DELIVERED" -> Color(0xFF4CAF50)
    "CANCELLED" -> Color(0xFFE53935)
    else        -> null
}

@Composable
private fun SellerOrderCard(order: SellerOrderResponse, onStatusUpdate: (String) -> Unit) {
    val nextStatus = nextSellerStatus(order.status)
    val chipColor = statusColor(order.status)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Заказ #${order.orderId}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (chipColor ?: colorDarkOrange).copy(alpha = 0.15f)
                ) {
                    Text(
                        statusLabel(order.status),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 11.sp,
                        color = chipColor ?: colorDarkOrange
                    )
                }
            }
            if (!order.deliveryAddress.isNullOrBlank()) {
                Text(
                    order.deliveryAddress ?: "",
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "${item.coffeeName} (${item.selectedSize}) × ${item.quantity}",
                        fontFamily = SoraFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${item.totalPrice}₽",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Итого: ${order.itemsTotal}₽",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 15.sp,
                    color = colorDarkOrange
                )
                if (nextStatus != null) {
                    Button(
                        onClick = { onStatusUpdate(nextStatus) },
                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "→ ${statusLabel(nextStatus)}",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopFormDialog(
    title: String,
    initial: SellerResponse?,
    isUploading: Boolean,
    onUploadImage: (MultipartBody.Part, (String?) -> Unit) -> Unit,
    onConfirm: (SellerRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var website by remember { mutableStateOf(initial?.website ?: "") }
    var imageUrl by remember { mutableStateOf(initial?.logoImage ?: "") }
    var previewUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            previewUri = it
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                    val ext = when (mimeType) { "image/png" -> "png"; "image/gif" -> "gif"; "image/webp" -> "webp"; else -> "jpg" }
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@launch
                    val body = bytes.toRequestBody(mimeType.toMediaType())
                    val part = MultipartBody.Part.createFormData("file", "upload.$ext", body)
                    onUploadImage(part) { uploaded -> if (uploaded != null) imageUrl = uploaded }
                } catch (_: Exception) {}
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.TwoTone.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                HorizontalDivider()
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Баннер
                    Text("Баннер магазина *", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { imageLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        val displayModel: Any? = previewUri ?: initial?.logoImage?.takeIf { it.isNotBlank() }
                        if (displayModel != null) {
                            AsyncImage(
                                model = if (previewUri != null) ImageRequest.Builder(context).data(previewUri).build() else displayModel,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            if (isUploading) {
                                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                                }
                            }
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(30.dp), tint = colorDarkOrange)
                                Text("Нажмите чтобы выбрать баннер", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    if (imageUrl.isNotBlank()) {
                        Text("✓ Баннер загружен", fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange)
                    } else {
                        Text("Обязательное поле", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }

                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Название магазина", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Описание", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = category, onValueChange = { category = it },
                        label = { Text("Категория", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = phone, onValueChange = { phone = it },
                        label = { Text("Телефон *", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = website, onValueChange = { website = it },
                        label = { Text("Сайт (необязательно)", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Отмена", fontFamily = SoraFontFamily) }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && description.isNotBlank() && category.isNotBlank() && phone.isNotBlank() && imageUrl.isNotBlank())
                                onConfirm(SellerRequest(name.trim(), description.trim(), category.trim(), phone.trim(), website.trim().ifBlank { null }, imageUrl))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Сохранить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600) }
                }
            }
        }
    }
}

@Composable
private fun BranchesTab(branches: List<BranchResponse>, viewModel: SellerViewModel) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingBranch by remember { mutableStateOf<BranchResponse?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Филиалы (${branches.size})",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Добавить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp)
            }
        }

        if (branches.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.TwoTone.Place,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Филиалы ещё не добавлены",
                        fontFamily = SoraFontFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Добавьте первый филиал вашего магазина",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(branches) { branch ->
                    BranchCard(
                        branch = branch,
                        onEdit = { editingBranch = branch },
                        onToggle = { viewModel.toggleBranch(branch.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        AddBranchScreen(
            onDismiss = { showCreateDialog = false },
            onConfirm = { req -> viewModel.createBranch(req); showCreateDialog = false }
        )
    }

    editingBranch?.let { branch ->
        BranchFormDialog(
            title = "Редактировать филиал",
            initial = branch,
            onConfirm = { req -> viewModel.updateBranch(branch.id, req); editingBranch = null },
            onDismiss = { editingBranch = null }
        )
    }
}

@Composable
private fun BranchStatusBadge(status: String) {
    val (color, label) = when (status) {
        "APPROVED" -> Color(0xFF4CAF50) to "Одобрен"
        "PENDING" -> Color(0xFFF59E0B) to "На проверке"
        else -> MaterialTheme.colorScheme.error to "Отклонён"
    }
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
private fun BranchCard(
    branch: BranchResponse,
    onEdit: () -> Unit,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            branch.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        BranchStatusBadge(branch.status)
                    }
                    if (!branch.rejectionReason.isNullOrBlank()) {
                        Text(
                            "Причина: ${branch.rejectionReason}",
                            fontFamily = SoraFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(
                        "${branch.city}, ${branch.address}",
                        fontFamily = SoraFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (branch.status != "PENDING") {
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.TwoTone.Create, "Редактировать", tint = colorDarkOrange)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!branch.workingHours.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.TwoTone.Place,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                branch.workingHours,
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.TwoTone.ShoppingCart,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Доставка: ${branch.deliveryFee.toInt()}₽  ·  Мин. заказ: ${branch.minOrderAmount.toInt()}₽",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!branch.managerEmail.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.TwoTone.Person,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            branch.managerEmail,
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (branch.isActive) colorDarkOrange.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.clickable { onToggle() }
                ) {
                    Text(
                        if (branch.isActive) "Активен" else "Неактивен",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 12.sp,
                        color = if (branch.isActive) colorDarkOrange else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun YandexMapPicker(
    initialLat: Double = 55.751244,
    initialLon: Double = 37.618423,
    targetPoint: Pair<Double, Double>? = null,
    onCameraIdle: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mapView = remember { YMapView(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val onCameraIdleLatest = rememberUpdatedState(onCameraIdle)

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> { MapKitFactory.getInstance().onStart(); mapView.onStart() }
                Lifecycle.Event.ON_STOP -> { mapView.onStop(); MapKitFactory.getInstance().onStop() }
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    val lastTargetRef = remember { arrayOfNulls<Pair<Double, Double>>(1) }

    Box(modifier = modifier) {
        AndroidView(
            factory = {
                mapView.apply {
                    map.move(CameraPosition(Point(initialLat, initialLon), 12f, 0f, 0f))
                    map.addCameraListener(object : CameraListener {
                        override fun onCameraPositionChanged(
                            p0: YMap, pos: CameraPosition,
                            reason: CameraUpdateReason, finished: Boolean
                        ) {
                            onCameraIdleLatest.value(pos.target.latitude, pos.target.longitude)
                        }
                    })
                }
            },
            update = { view ->
                // Двигаем камеру только если targetPoint реально изменился
                if (targetPoint != null && targetPoint != lastTargetRef[0]) {
                    lastTargetRef[0] = targetPoint
                    val (la, lo) = targetPoint
                    view.map.move(CameraPosition(Point(la, lo), 15f, 0f, 0f))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Icon(
            imageVector = Icons.TwoTone.Place,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-18).dp)
                .size(40.dp),
            tint = colorDarkOrange
        )
    }
}

@Composable
private fun AddBranchScreen(
    onDismiss: () -> Unit,
    onConfirm: (BranchRequest) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    var lat by remember { mutableDoubleStateOf(55.751244) }
    var lon by remember { mutableDoubleStateOf(37.618423) }
    var city by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var detectedAddress by remember { mutableStateOf("") }
    var mapSearchQuery by remember { mutableStateOf("") }
    var targetPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var searchLoading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf("") }
    val httpClient = remember {
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder().header("User-Agent", "CoffeeMarketplace/1.0").build())
            }.build()
    }
    val scope = rememberCoroutineScope()

    val backendBase = "http://10.0.2.2:8080/api/geocode"

    // Прямое геокодирование: поисковая строка → карта
    LaunchedEffect(mapSearchQuery) {
        searchError = ""
        searchLoading = false
        if (mapSearchQuery.length >= 3) {
            delay(800)
            if (!isActive) return@LaunchedEffect
            searchLoading = true
            val encoded = java.net.URLEncoder.encode(mapSearchQuery, "UTF-8")
            val url = "$backendBase/search?q=$encoded"
            val call = httpClient.newCall(okhttp3.Request.Builder().url(url).build())
            try {
                val body = withContext(Dispatchers.IO) { call.execute().body?.string() }
                if (!isActive) return@LaunchedEffect
                if (body.isNullOrBlank()) { searchError = "Нет ответа от сервера"; return@LaunchedEffect }
                val arr = org.json.JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    lat = obj.getString("lat").toDouble()
                    lon = obj.getString("lon").toDouble()
                    targetPoint = lat to lon
                    searchError = ""
                    val addr = obj.optJSONObject("address")
                    if (addr != null) {
                        val c = addr.optString("city")
                            .ifBlank { addr.optString("town") }
                            .ifBlank { addr.optString("village") }
                            .ifBlank { addr.optString("municipality") }
                        val r = addr.optString("road")
                        val h = addr.optString("house_number")
                        if (c.isNotBlank()) city = c
                        if (r.isNotBlank()) street = r
                        if (h.isNotBlank()) house = h
                    }
                    val full = obj.optString("display_name")
                    if (full.isNotBlank()) detectedAddress = full
                } else {
                    searchError = "Адрес не найден"
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                call.cancel()
                throw e
            } catch (e: Exception) {
                searchError = "Ошибка: ${e.message}"
            } finally {
                searchLoading = false
            }
        }
    }


    var name by remember { mutableStateOf("") }
    var workingHours by remember { mutableStateOf("") }
    var deliveryFee by remember { mutableStateOf("0") }
    var minOrderAmount by remember { mutableStateOf("0") }

    var managerEmail by remember { mutableStateOf("") }
    var managerPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Шапка ──────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = { if (step == 0) onDismiss() else step-- }) {
                        Icon(Icons.TwoTone.ArrowBack, null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            listOf("Местоположение", "Детали филиала", "Аккаунт")[step],
                            fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 18.sp
                        )
                        Text(
                            "Шаг ${step + 1} из 3",
                            fontFamily = SoraFontFamily, fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Индикатор прогресса ────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { idx ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (idx <= step) colorDarkOrange else MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                // ── Контент шага ───────────────────────────────────────────
                when (step) {
                    0 -> {
                        Column(modifier = Modifier.weight(1f)) {
                            // Поиск адреса
                            OutlinedTextField(
                                value = mapSearchQuery,
                                onValueChange = { mapSearchQuery = it },
                                placeholder = { Text("Найти адрес...", fontFamily = SoraFontFamily) },
                                leadingIcon = {
                                    if (searchLoading)
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colorDarkOrange)
                                    else
                                        Icon(Icons.TwoTone.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                trailingIcon = {
                                    if (mapSearchQuery.isNotBlank()) {
                                        IconButton(onClick = { mapSearchQuery = ""; searchError = "" }) {
                                            Icon(Icons.TwoTone.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                            )
                            if (searchError.isNotBlank()) {
                                Text(
                                    searchError,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontFamily = SoraFontFamily,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            // Карта
                            YandexMapPicker(
                                initialLat = lat, initialLon = lon,
                                targetPoint = targetPoint,
                                onCameraIdle = { la, lo -> lat = la; lon = lo },
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            )
                            // Панель адреса (как в Delivery Club)
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shadowElevation = 12.dp,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    if (detectedAddress.isNotBlank()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(Icons.TwoTone.Place, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = colorDarkOrange)
                                            Text(
                                                detectedAddress,
                                                fontFamily = SoraFontFamily, fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        HorizontalDivider()
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = city, onValueChange = { city = it },
                                            label = { Text("Город", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                            singleLine = true, modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                        )
                                        OutlinedTextField(
                                            value = street, onValueChange = { street = it },
                                            label = { Text("Улица", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                            singleLine = true, modifier = Modifier.weight(1.8f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = house, onValueChange = { house = it },
                                            label = { Text("Дом", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                            singleLine = true, modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                        )
                                        OutlinedTextField(
                                            value = building, onValueChange = { building = it },
                                            label = { Text("Корпус/стр.", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                            placeholder = { Text("необяз.", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                            singleLine = true, modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                        )
                                    }
                                    Button(
                                        onClick = { step = 1 },
                                        enabled = city.isNotBlank() && street.isNotBlank() && house.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth().height(50.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text("Подтвердить адрес", fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 15.sp)
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            OutlinedTextField(
                                value = name, onValueChange = { name = it },
                                label = { Text("Название филиала", fontFamily = SoraFontFamily) },
                                placeholder = { Text("Например: Центральный", fontFamily = SoraFontFamily) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                            )
                            val workingHoursRegex = Regex("""^\d{1,2}:\d{2}[–\-—]\d{1,2}:\d{2}$""")
                            val workingHoursError = workingHours.isNotBlank() && !workingHoursRegex.matches(workingHours)
                            OutlinedTextField(
                                value = workingHours, onValueChange = { workingHours = it },
                                label = { Text("Часы работы", fontFamily = SoraFontFamily) },
                                placeholder = { Text("09:00–22:00", fontFamily = SoraFontFamily) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                isError = workingHoursError,
                                supportingText = if (workingHoursError) {
                                    { Text("Формат: 09:00–22:00", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = deliveryFee,
                                    onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d{0,8}"))) deliveryFee = it },
                                    label = { Text("Доставка ₽", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                    singleLine = true, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                )
                                OutlinedTextField(
                                    value = minOrderAmount,
                                    onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d{0,8}"))) minOrderAmount = it },
                                    label = { Text("Мин. заказ ₽", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                                    singleLine = true, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { step = 2 },
                                enabled = name.trim().length >= 2 && !workingHoursError,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Далее", fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 15.sp)
                            }
                        }
                    }
                    2 -> {
                        val emailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
                        val emailError = managerEmail.isNotBlank() && !emailRegex.matches(managerEmail)
                        val passwordError = managerPassword.isNotBlank() && managerPassword.length < 6
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                "Создайте аккаунт для входа в приложение филиала",
                                fontFamily = SoraFontFamily, fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = managerEmail, onValueChange = { managerEmail = it },
                                label = { Text("Логин (Email)", fontFamily = SoraFontFamily) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                isError = emailError,
                                supportingText = if (emailError) {
                                    { Text("Введите корректный email", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                            )
                            OutlinedTextField(
                                value = managerPassword, onValueChange = { managerPassword = it },
                                label = { Text("Пароль", fontFamily = SoraFontFamily) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                isError = passwordError,
                                supportingText = if (passwordError) {
                                    { Text("Минимум 6 символов", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                                       else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(if (passwordVisible) Icons.TwoTone.Info else Icons.TwoTone.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onConfirm(
                                        BranchRequest(
                                            name = name,
                                            address = listOf(street, house, building).filter { it.isNotBlank() }.joinToString(", "),
                                            city = city,
                                            latitude = lat,
                                            longitude = lon,
                                            deliveryFee = deliveryFee.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO,
                                            minOrderAmount = minOrderAmount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO,
                                            workingHours = workingHours.ifBlank { null },
                                            managerEmail = managerEmail,
                                            managerPassword = managerPassword
                                        )
                                    )
                                },
                                enabled = managerEmail.isNotBlank() && !emailError && managerPassword.length >= 6 && !passwordError,
                                modifier = Modifier.fillMaxWidth().height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Отправить на модерацию", fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 15.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BranchFormDialog(
    title: String,
    initial: BranchResponse?,
    onConfirm: (BranchRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val isCreating = initial == null
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var address by remember { mutableStateOf(initial?.address ?: "") }
    var city by remember { mutableStateOf(initial?.city ?: "") }
    var workingHours by remember { mutableStateOf(initial?.workingHours ?: "") }
    var deliveryFee by remember { mutableStateOf(initial?.deliveryFee?.toInt()?.toString() ?: "199") }
    var minOrderAmount by remember { mutableStateOf(initial?.minOrderAmount?.toInt()?.toString() ?: "500") }
    var managerEmail by remember { mutableStateOf("") }
    var managerPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        title,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.TwoTone.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Название филиала", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = city, onValueChange = { city = it },
                        label = { Text("Город", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    OutlinedTextField(
                        value = address, onValueChange = { address = it },
                        label = { Text("Адрес", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    val whRegex = Regex("""^\d{1,2}:\d{2}[–\-—]\d{1,2}:\d{2}$""")
                    val whError = workingHours.isNotBlank() && !whRegex.matches(workingHours)
                    OutlinedTextField(
                        value = workingHours, onValueChange = { workingHours = it },
                        label = { Text("Часы работы (напр. 09:00–22:00)", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        isError = whError,
                        supportingText = if (whError) {
                            { Text("Формат: 09:00–22:00", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = deliveryFee,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d{0,8}"))) deliveryFee = it },
                            label = { Text("Доставка ₽", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                        )
                        OutlinedTextField(
                            value = minOrderAmount,
                            onValueChange = { if (it.isEmpty() || it.matches(Regex("\\d{0,8}"))) minOrderAmount = it },
                            label = { Text("Мин. заказ ₽", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                        )
                    }

                    if (isCreating) {
                        HorizontalDivider()
                        Text(
                            "Аккаунт менеджера филиала",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Менеджер будет входить с этими данными и видеть только заказы этого филиала",
                            fontFamily = SoraFontFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dlgEmailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
                        val dlgEmailError = managerEmail.isNotBlank() && !dlgEmailRegex.matches(managerEmail)
                        val dlgPwdError = managerPassword.isNotBlank() && managerPassword.length < 6
                        OutlinedTextField(
                            value = managerEmail,
                            onValueChange = { managerEmail = it },
                            label = { Text("Email менеджера", fontFamily = SoraFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = dlgEmailError,
                            supportingText = if (dlgEmailError) {
                                { Text("Введите корректный email", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                        )
                        OutlinedTextField(
                            value = managerPassword,
                            onValueChange = { managerPassword = it },
                            label = { Text("Пароль менеджера", fontFamily = SoraFontFamily) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = dlgPwdError,
                            supportingText = if (dlgPwdError) {
                                { Text("Минимум 6 символов", fontFamily = SoraFontFamily, fontSize = 11.sp) }
                            } else null,
                            shape = RoundedCornerShape(12.dp),
                            visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None
                                                   else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        if (passwordVisible) Icons.TwoTone.Info else Icons.TwoTone.Lock,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = colorDarkOrange, focusedLabelColor = colorDarkOrange)
                        )
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Отмена", fontFamily = SoraFontFamily) }

                    val saveEmailRegex = Regex("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$")
                    val saveEnabled = name.trim().length >= 2 && address.isNotBlank() && city.isNotBlank() &&
                        (workingHours.isBlank() || Regex("""^\d{1,2}:\d{2}[–\-—]\d{1,2}:\d{2}$""").matches(workingHours)) &&
                        (!isCreating || (managerEmail.isNotBlank() && saveEmailRegex.matches(managerEmail) && managerPassword.length >= 6))
                    Button(
                        onClick = {
                            onConfirm(
                                BranchRequest(
                                    name = name.trim(),
                                    address = address.trim(),
                                    city = city.trim(),
                                    workingHours = workingHours.trim().ifBlank { null },
                                    deliveryFee = deliveryFee.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    minOrderAmount = minOrderAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                                    managerEmail = if (isCreating) managerEmail.trim() else null,
                                    managerPassword = if (isCreating) managerPassword else null
                                )
                            )
                        },
                        enabled = saveEnabled,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Сохранить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600) }
                }
            }
        }
    }
}

private data class VariantState(val size: String, val price: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductFormDialog(
    title: String,
    initial: ProductResponse?,
    categories: List<ProductCategoryResponse>,
    isUploading: Boolean,
    onUploadImage: (MultipartBody.Part, (String?) -> Unit) -> Unit,
    onConfirm: (ProductManageRequest) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var selectedCategoryId by remember { mutableStateOf(initial?.type?.id ?: 1) }
    var imageUrl by remember { mutableStateOf(initial?.imageUrl ?: "") }
    var previewUri by remember { mutableStateOf<Uri?>(null) }
    var variants by remember {
        mutableStateOf(
            initial?.sizes?.map { VariantState(it.size, it.price.toInt().toString()) }
                ?: listOf(VariantState("S", ""), VariantState("M", ""), VariantState("L", ""))
        )
    }
    var categoryExpanded by remember { mutableStateOf(false) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            previewUri = it
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                    val ext = when (mimeType) {
                        "image/png"  -> "png"
                        "image/gif"  -> "gif"
                        "image/webp" -> "webp"
                        else         -> "jpg"
                    }
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@launch
                    val body = bytes.toRequestBody(mimeType.toMediaType())
                    val part = MultipartBody.Part.createFormData("file", "upload.$ext", body)
                    onUploadImage(part) { uploaded -> if (uploaded != null) imageUrl = uploaded }
                } catch (_: Exception) {}
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.TwoTone.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                // Scrollable form
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Название", fontFamily = SoraFontFamily) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text("Описание", fontFamily = SoraFontFamily) },
                        modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category dropdown
                    if (categories.isNotEmpty()) {
                        ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                            OutlinedTextField(
                                value = categories.find { it.id == selectedCategoryId }?.type ?: "",
                                onValueChange = {}, readOnly = true,
                                label = { Text("Категория", fontFamily = SoraFontFamily) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat.type, fontFamily = SoraFontFamily) },
                                        onClick = { selectedCategoryId = cat.id; categoryExpanded = false }
                                    )
                                }
                            }
                        }
                    }

                    // Image upload
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Изображение", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)

                        // Preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { imageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(context).data(previewUri).build()
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                if (isUploading) {
                                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(36.dp), tint = colorDarkOrange)
                                    Text("Нажмите чтобы выбрать фото", fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        if (imageUrl.isNotEmpty()) {
                            Text("✓ Фото загружено", fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange)
                        }
                    }

                    // Variants
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Варианты", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            TextButton(onClick = { variants = variants + VariantState("", "") }) {
                                Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(16.dp), tint = colorDarkOrange)
                                Spacer(Modifier.width(4.dp))
                                Text("Добавить", fontFamily = SoraFontFamily, fontSize = 13.sp, color = colorDarkOrange)
                            }
                        }

                        variants.forEachIndexed { index, variant ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = variant.size,
                                    onValueChange = { v -> variants = variants.toMutableList().also { it[index] = variant.copy(size = v) } },
                                    label = { Text("Размер", fontFamily = SoraFontFamily, fontSize = 11.sp) },
                                    singleLine = true, modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = variant.price,
                                    onValueChange = { v ->
                                        if (v.isEmpty() || v.matches(Regex("\\d{0,6}(\\.\\d{0,2})?")))
                                            variants = variants.toMutableList().also { it[index] = variant.copy(price = v) }
                                    },
                                    label = { Text("Цена ₽", fontFamily = SoraFontFamily, fontSize = 11.sp) },
                                    singleLine = true, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)
                                )
                                if (variants.size > 1) {
                                    IconButton(onClick = { variants = variants.toMutableList().also { it.removeAt(index) } }) {
                                        Icon(Icons.TwoTone.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                } else {
                                    Spacer(Modifier.size(48.dp))
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Отмена", fontFamily = SoraFontFamily) }

                    Button(
                        onClick = {
                            val validVariants = variants.filter { it.size.isNotBlank() && it.price.isNotBlank() }
                            if (name.isNotBlank() && description.isNotBlank() && imageUrl.isNotBlank() && validVariants.isNotEmpty()) {
                                onConfirm(
                                    ProductManageRequest(
                                        name = name.trim(),
                                        description = description.trim(),
                                        categoryId = selectedCategoryId,
                                        imageUrl = imageUrl.trim(),
                                        variants = validVariants.map {
                                            VariantRequest(it.size.trim(), it.price.toBigDecimalOrNull() ?: BigDecimal.ZERO)
                                        }
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Сохранить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600) }
                }
            }
        }
    }
}

@Composable
private fun ModerationPendingContent(navController: NavController, onRefresh: () -> Unit) {
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 56.dp, start = 8.dp, end = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.medium).clickable { navController.navigateUp() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.leftarrow),
                        contentDescription = "Назад",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(50.dp)).background(colorDarkOrange.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.TwoTone.CheckCircle, null, tint = colorDarkOrange, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Заявка на рассмотрении",
                fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Ваш магазин проходит проверку нашими модераторами.",
                fontFamily = SoraFontFamily, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, lineHeight = 21.sp
            )
            Spacer(Modifier.height(32.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFF8E1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    ModerationInfoRow(Icons.TwoTone.CheckCircle, Color(0xFFFFB300), "Заявка успешно принята")
                    ModerationInfoRow(Icons.TwoTone.Info, Color(0xFFFFB300), "Модерация занимает до 24 часов")
                    ModerationInfoRow(Icons.TwoTone.CheckCircle, Color(0xFFFFB300), "После одобрения магазин появится в каталоге")
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Проверить статус", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun ModerationRejectedContent(
    navController: NavController,
    shop: SellerResponse,
    isUploading: Boolean,
    onUploadImage: (MultipartBody.Part, (String?) -> Unit) -> Unit,
    onResubmit: (SellerRequest) -> Unit,
    onRefresh: () -> Unit
) {
    var showResubmitDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(top = 56.dp, start = 8.dp, end = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(44.dp).clip(MaterialTheme.shapes.medium).clickable { navController.navigateUp() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.leftarrow),
                        contentDescription = "Назад",
                        modifier = Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Box(
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.TwoTone.Info, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(52.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Заявка отклонена",
                fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "К сожалению, ваш магазин не прошёл проверку",
                fontFamily = SoraFontFamily, fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Причина отклонения", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                    Text(
                        shop.rejectionReason ?: "Причина не указана",
                        fontFamily = SoraFontFamily, fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer, lineHeight = 20.sp
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.TwoTone.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Text(
                        "Исправьте указанные замечания и обратитесь в поддержку для повторного рассмотрения заявки.",
                        fontFamily = SoraFontFamily, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp
                    )
                }
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { showResubmitDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Подать заново", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRefresh,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Проверить статус", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp)
            }
        }
    }

    if (showResubmitDialog) {
        ShopFormDialog(
            title = "Повторная заявка",
            initial = shop,
            isUploading = isUploading,
            onUploadImage = onUploadImage,
            onConfirm = { req ->
                onResubmit(req)
                showResubmitDialog = false
            },
            onDismiss = { showResubmitDialog = false }
        )
    }
}

@Composable
private fun ModerationInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Text(text, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, lineHeight = 18.sp)
    }
}
