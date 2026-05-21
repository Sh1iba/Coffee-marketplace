package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.coffeeshop.R
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.ReviewResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.data.repository.ProductRepository
import com.example.coffeeshop.data.repository.ReviewRepository
import com.example.coffeeshop.data.repository.SellerRepository
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorLightGrey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class SellerStoreViewModel @Inject constructor(
    private val sellerRepository: SellerRepository,
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _seller = MutableStateFlow<SellerResponse?>(null)
    val seller: StateFlow<SellerResponse?> = _seller

    private val _products = MutableStateFlow<List<ProductResponse>>(emptyList())
    val products: StateFlow<List<ProductResponse>> = _products

    private val _reviews = MutableStateFlow<List<ReviewResponse>>(emptyList())
    val reviews: StateFlow<List<ReviewResponse>> = _reviews

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun load(sellerId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _seller.value = sellerRepository.getSellerById(sellerId)
            try {
                val all = productRepository.getAllProducts()
                _products.value = all.filter { it.sellerId == sellerId }
            } catch (_: Exception) {}
            _reviews.value = reviewRepository.getSellerReviews(sellerId)
            _isLoading.value = false
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerStoreScreen(navController: NavController, sellerId: Long) {
    val viewModel: SellerStoreViewModel = hiltViewModel()

    val seller    by viewModel.seller.collectAsState()
    val products  by viewModel.products.collectAsState()
    val reviews   by viewModel.reviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery      by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Все") }

    val categories = remember(products) {
        listOf("Все") + products.map { it.type.type }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products
            .filter { if (selectedCategory == "Все") true else it.type.type == selectedCategory }
            .filter { if (searchQuery.isBlank()) true else it.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(sellerId) { viewModel.load(sellerId) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
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
                        text = seller?.name ?: "Магазин",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Box(modifier = Modifier.size(44.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorDarkOrange)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Шапка магазина
                seller?.let { s ->
                    item {
                        SellerStoreHeader(
                            seller = s,
                            productCount = products.size,
                            reviewCount = reviews.size,
                            onReviewsClick = {
                                navController.navigate("${NavigationRoutes.SELLER_REVIEWS}/$sellerId")
                            }
                        )
                    }
                }

                // Поиск
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск в магазине...", fontFamily = SoraFontFamily, fontSize = 14.sp) },
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
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorDarkOrange,
                            focusedLabelColor = colorDarkOrange
                        )
                    )
                }

                // Фильтр по категории
                if (categories.size > 2) {
                    item {
                        Row(
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontFamily = SoraFontFamily, fontSize = 13.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colorDarkOrange,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Заголовок товаров
                item {
                    Text(
                        text = if (searchQuery.isBlank() && selectedCategory == "Все")
                            "Товары (${products.size})"
                        else
                            "Найдено: ${filteredProducts.size}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                if (products.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.TwoTone.ShoppingCart, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Товаров пока нет", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else if (filteredProducts.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                            Text("Ничего не найдено", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    items(filteredProducts) { coffee ->
                        SellerProductCard(
                            coffee = coffee,
                            onClick = {
                                val sizesEncoded = URLEncoder.encode(
                                    coffee.sizes.joinToString(",") { "${it.size}:${it.price}:${it.volume ?: ""}" },
                                    "UTF-8"
                                )
                                val imageUrlEncoded = URLEncoder.encode(coffee.imageUrl, "UTF-8")
                                navController.navigate(
                                    "${NavigationRoutes.DETAIL}/${coffee.id}/${coffee.name}/${coffee.type.type}/${coffee.description}?imageUrl=$imageUrlEncoded&sizes=$sizesEncoded&favoriteSize=&sellerId=${coffee.sellerId ?: -1L}"
                                )
                            }
                        )
                    }
                }

                // Отзывы
                if (reviews.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Отзывы (${reviews.size})",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            TextButton(onClick = {
                                navController.navigate("${NavigationRoutes.SELLER_REVIEWS}/$sellerId")
                            }) {
                                Text(
                                    "Все",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 13.sp,
                                    color = colorDarkOrange
                                )
                                Icon(
                                    Icons.TwoTone.ArrowForward,
                                    contentDescription = null,
                                    tint = colorDarkOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    items(reviews.take(3)) { review ->
                        ReviewCard(review = review)
                    }
                    if (reviews.size > 3) {
                        item {
                            TextButton(
                                onClick = {
                                    navController.navigate("${NavigationRoutes.SELLER_REVIEWS}/$sellerId")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    "Показать все ${reviews.size} отзывов",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 13.sp,
                                    color = colorDarkOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SellerStoreHeader(
    seller: SellerResponse,
    productCount: Int = 0,
    reviewCount: Int = 0,
    onReviewsClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Баннер-фон
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Color(0xFF313131), Color(0xFF111111)))
                )
        ) {
            if (!seller.logoImage.isNullOrEmpty()) {
                AsyncImage(
                    model = seller.logoImage,
                    contentDescription = seller.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Затемняющий градиент снизу чтобы карточка читалась
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                            )
                        )
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.TwoTone.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            seller.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W700,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Инфо
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-20).dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorDarkOrange.copy(alpha = 0.12f)
                ) {
                    Text(
                        seller.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = colorDarkOrange,
                        fontWeight = FontWeight.W600
                    )
                }

                Text(
                    seller.description,
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                // Статистика
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StoreStatItem(icon = Icons.TwoTone.ShoppingCart, value = productCount.toString(), label = "Товаров")
                    StoreStatItem(
                        icon = Icons.TwoTone.Star,
                        value = if (reviewCount > 0) "%.1f ($reviewCount)".format(seller.rating) else "%.1f".format(seller.rating),
                        label = "смотреть >",
                        onClick = onReviewsClick
                    )
                    StoreStatItem(icon = Icons.TwoTone.Person, value = seller.ownerName, label = "Продавец")
                }
            }
        }
    }
}

@Composable
private fun StoreStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = if (onClick != null) Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        else Modifier
    ) {
        Icon(icon, null, tint = colorDarkOrange, modifier = Modifier.size(20.dp))
        Text(value, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            label,
            fontFamily = SoraFontFamily,
            fontSize = 10.sp,
            color = if (onClick != null) colorDarkOrange else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun ReviewCard(review: ReviewResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.userName,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                    repeat(review.rating) {
                        Icon(Icons.TwoTone.Star, null, tint = colorDarkOrange, modifier = Modifier.size(14.dp))
                    }
                }
            }
            if (!review.comment.isNullOrBlank()) {
                Text(
                    review.comment,
                    fontFamily = SoraFontFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
            Text(
                review.createdAt.take(10).replace("-", "."),
                fontFamily = SoraFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SellerProductCard(
    coffee: ProductResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF3A3A3A))
            ) {
                AsyncImage(
                    model = coffee.imageUrl,
                    contentDescription = coffee.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    coffee.name,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    coffee.type.type,
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    color = colorDarkOrange
                )
                val minPrice = coffee.sizes.minOfOrNull { it.price }?.toInt() ?: 0
                Text(
                    "от ${minPrice}₽",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W700,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Icon(
                Icons.TwoTone.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
