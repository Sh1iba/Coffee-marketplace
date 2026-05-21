package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.net.URLEncoder
import com.example.coffeeshop.R
import androidx.compose.foundation.lazy.LazyRow
import com.example.coffeeshop.data.remote.response.CartItemResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.viewmodel.CartViewModel
import com.google.gson.Gson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {
    val viewModel: CartViewModel = hiltViewModel()

    val cartItems by viewModel.cartItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sellers by viewModel.sellers.collectAsState()

    val storeList = remember(sellers, cartItems) {
        cartItems.mapNotNull { it.sellerId }.distinct()
            .mapNotNull { id -> sellers[id]?.let { id to it } }
    }

    var selectedStoreId by remember(storeList) {
        mutableStateOf(storeList.firstOrNull()?.first)
    }

    val visibleItems = remember(cartItems, selectedStoreId) {
        if (selectedStoreId == null) cartItems
        else cartItems.filter { it.sellerId == selectedStoreId }
    }

    var selectedKeys by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }

    val selectedCartItems = remember(visibleItems, selectedKeys) {
        visibleItems.filter { item ->
            val key = "${item.id}_${item.selectedSize}"
            selectedKeys.contains(key)
        }
    }

    val selectedTotalPrice = remember(selectedCartItems) {
        selectedCartItems.sumOf { it.totalPrice.toDouble() }
    }

    val selectedTotalItems = remember(selectedCartItems) {
        selectedCartItems.sumOf { it.quantity }
    }

    val allSelected = remember(visibleItems, selectedKeys) {
        visibleItems.isNotEmpty() && visibleItems.all { selectedKeys.contains("${it.id}_${it.selectedSize}") }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "Корзина",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                            color = colorScheme.onBackground
                        )
                    },
                    modifier = Modifier.padding(top = 68.dp),
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = colorScheme.background
                    )
                )

                if (cartItems.isNotEmpty()) {
                    // Горизонтальный скролл табов по магазинам
                    if (storeList.size > 1) {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(storeList) { (id, seller) ->
                                val isSelected = selectedStoreId == id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) colorDarkOrange else colorDarkOrange.copy(alpha = 0.10f))
                                        .clickable {
                                            selectedStoreId = id
                                            selectedKeys = emptySet()
                                        }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = seller.name,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.W600,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else colorDarkOrange
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .background(colorScheme.background),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                if (allSelected) {
                                    selectedKeys = emptySet()
                                } else {
                                    selectedKeys = visibleItems.map { "${it.id}_${it.selectedSize}" }.toSet()
                                }
                            }
                        ) {
                            Checkbox(
                                checked = allSelected,
                                onCheckedChange = { isChecked ->
                                    selectedKeys = if (isChecked) {
                                        visibleItems.map { "${it.id}_${it.selectedSize}" }.toSet()
                                    } else {
                                        emptySet()
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = colorDarkOrange,
                                    uncheckedColor = Color.Gray
                                )
                            )
                            Text(
                                text = "Выбрать все",
                                fontWeight = FontWeight.W500,
                                fontSize = 16.sp,
                                color = colorScheme.onBackground,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        if (selectedKeys.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    selectedKeys.forEach { key ->
                                        val parts = key.split("_")
                                        if (parts.size == 2) {
                                            val coffeeId = parts[0].toIntOrNull()
                                            val selectedSize = parts[1]
                                            if (coffeeId != null) {
                                                viewModel.removeFromCart(coffeeId, selectedSize)
                                            }
                                        }
                                    }
                                    selectedKeys = emptySet()
                                }
                            ) {
                                Text(
                                    text = "Удалить",
                                    color = colorDarkOrange,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = colorScheme.background
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colorDarkOrange)
                }
            }
            error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Ошибка загрузки",
                        color = colorScheme.error,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error!!,
                        color = colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.loadCart()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorDarkOrange
                        )
                    ) {
                        Text("Повторить")
                    }
                }
            }
            cartItems.isEmpty() && !isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    EmptyCartState(navController)
                }
            }
            else -> {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(visibleItems) { item ->
                            CartItemCard(
                                item = item,
                                viewModel = viewModel,
                                navController = navController,
                                isSelected = selectedKeys.contains("${item.id}_${item.selectedSize}"),
                                onSelectionChange = { isSelected ->
                                    val key = "${item.id}_${item.selectedSize}"
                                    selectedKeys = if (isSelected) {
                                        selectedKeys + key
                                    } else {
                                        selectedKeys - key
                                    }
                                },
                                onQuantityChange = { newQuantity ->
                                    if (newQuantity > 0) {
                                        viewModel.updateQuantity(item.id, item.selectedSize, newQuantity)
                                    } else {
                                        viewModel.removeFromCart(item.id, item.selectedSize)
                                    }
                                },
                                onRemove = {
                                    viewModel.removeFromCart(item.id, item.selectedSize)
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OrderButton(
                        totalPrice = selectedTotalPrice,
                        totalItems = selectedTotalItems,
                        onOrderClick = {
                            if (selectedCartItems.isNotEmpty()) {
                                val gson = Gson()
                                val selectedItemsJson = gson.toJson(selectedCartItems)
                                val totalPrice = selectedTotalPrice

                                navController.navigate(
                                    "${NavigationRoutes.ORDER}?selectedItems=${URLEncoder.encode(selectedItemsJson, "UTF-8")}&totalPrice=$totalPrice"
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(15.dp))
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItemResponse,
    viewModel: CartViewModel,
    navController: NavController,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable {
                coroutineScope.launch {
                    val fullCoffeeData = viewModel.getFullProductData(item.id)
                    fullCoffeeData?.let { coffee ->
                        val sizesEncoded = URLEncoder.encode(
                            coffee.sizes.joinToString(",") { size ->
                                "${size.size}:${size.price}:${size.volume ?: ""}"
                            },
                            "UTF-8"
                        )
                        val imageUrlEncoded = URLEncoder.encode(coffee.imageUrl, "UTF-8")
                        navController.navigate(
                            "${NavigationRoutes.DETAIL}/" +
                                    "${coffee.id}/" +
                                    "${coffee.name}/" +
                                    "${coffee.type.type}/" +
                                    "${coffee.description}" +
                                    "?imageUrl=$imageUrlEncoded" +
                                    "&sizes=$sizesEncoded" +
                                    "&favoriteSize=${item.selectedSize}" +
                                    "&sellerId=${coffee.sellerId ?: -1L}"
                        )
                    }
                }
            },
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(60.dp)
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { isChecked ->
                        onSelectionChange(isChecked)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorDarkOrange,
                        uncheckedColor = Color.White
                    ),
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = (-4).dp, y = (-4).dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = colorScheme.outline
                )

                Text(
                    text = "Размер: ${item.selectedSize}",
                    fontWeight = FontWeight.W500,
                    fontSize = 13.sp,
                    color = colorScheme.outline,
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                Text(
                    text = "₽${"%.2f".format(item.totalPrice)}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = colorDarkOrange
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.minus_button),
                            contentDescription = "Уменьшить",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = item.quantity.toString(),
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.width(24.dp),
                        textAlign = TextAlign.Center,
                        color = colorScheme.outline
                    )

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.plus_button),
                            contentDescription = "Увеличить",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = colorDarkOrange
                    )
                }
            }
        }
    }
}

@Composable
fun OrderButton(totalPrice: Double, totalItems: Int, onOrderClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorDarkOrange)
            .clickable(
                enabled = totalItems > 0,
                onClick = onOrderClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (totalItems > 0) {
                "К оформлению $totalItems шт., ₽${"%.2f".format(totalPrice)}"
            } else {
                "Выберите товары"
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.W600,
            color = Color.White
        )
    }
}

@Composable
fun EmptyCartState(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEDE0D4).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                modifier = Modifier.size(48.dp),
                tint = colorDarkOrange
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Корзина пуста",
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            color = Color(0xFF4A4A4A),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Добавляйте понравившиеся напитки в корзину",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFFB0B0B0),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .height(48.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFD17C46))
                .clickable { navController.popBackStack() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Перейти в каталог",
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CartScreenPreview() {
    CoffeeShopTheme {
        CartScreen(navController = rememberNavController())
    }
}