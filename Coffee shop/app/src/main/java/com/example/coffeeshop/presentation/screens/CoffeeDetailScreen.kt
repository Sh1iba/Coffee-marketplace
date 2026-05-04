package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.coffeeshop.R
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.ProductVariantResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorLightGrey
import com.example.coffeeshop.presentation.theme.colorSelectOrange
import com.example.coffeeshop.presentation.viewmodel.CartViewModel
import com.example.coffeeshop.presentation.viewmodel.CoffeeDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoffeeDetailScreen(
    navController: NavController,
    coffee: ProductResponse,
    favoriteSize: String = ""
) {
    val viewModel: CoffeeDetailViewModel = hiltViewModel()

    val currentCoffee by viewModel.coffee.collectAsState()
    val isFavoriteWithCurrentSize by viewModel.isFavoriteWithCurrentSize.collectAsState()
    val imageBytes by viewModel.imageBytes.collectAsState()
    val selectedSize by viewModel.selectedSize.collectAsState()
    val currentPrice by viewModel.currentPrice.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val availableSizes by viewModel.availableSizes.collectAsState()
    val isInCartWithCurrentSize by viewModel.isInCartWithCurrentSize.collectAsState()

    LaunchedEffect(coffee) {
        viewModel.setCoffee(coffee, favoriteSize)
        viewModel.loadCoffeeImage()
        viewModel.checkIfInCartWithCurrentSize()
    }

    LaunchedEffect(selectedSize) {
        viewModel.checkIfInCartWithCurrentSize()
    }

    val onBackClick = { navController.popBackStack() }

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
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = com.example.coffeeshop.R.drawable.leftarrow),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                        )
                    }

                    Text(
                        text = "Подробно",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 19.2.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.toggleFavorite()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isFavoriteWithCurrentSize) {
                            Image(
                                painter = painterResource(id = com.example.coffeeshop.R.drawable.heart),
                                contentDescription = "Favorite",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(Color.Red)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = com.example.coffeeshop.R.drawable.heart),
                                contentDescription = "Favorite",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            BottomOrderPanel(
                price = currentPrice,
                isInCart = isInCartWithCurrentSize,
                onButtonClick = {
                    if (isInCartWithCurrentSize) {
                    } else {
                        viewModel.addToCart()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(327f / 202f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF727070))
                ) {
                    if (imageBytes != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(imageBytes)
                                    .build()
                            ),
                            contentDescription = currentCoffee?.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentCoffee?.name ?: coffee.name,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                CircularProgressIndicator(color = Color.White)
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .wrapContentHeight()
                    .padding(top = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = currentCoffee?.name ?: coffee.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                        lineHeight = 30.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = currentCoffee?.type?.type ?: coffee.type.type,
                        fontWeight = FontWeight.W400,
                        fontSize = 12.sp,
                        lineHeight = 14.4.sp,
                        color = colorLightGrey
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            ) {
                                append("⭐ 4.8 ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    color = colorLightGrey,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W400
                                )
                            ) {
                                append("(150)")
                            }
                        },
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        color = colorLightGrey
                    )
                }

                Box(
                    modifier = Modifier.height(85.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row(
                        modifier = Modifier
                            .wrapContentWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = com.example.coffeeshop.R.drawable.fast_delivery),
                            contentDescription = "Fast Delivery",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Image(
                            painter = painterResource(id = com.example.coffeeshop.R.drawable.quality_bean),
                            contentDescription = "Quality Bean",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )

                        Image(
                            painter = painterResource(id = com.example.coffeeshop.R.drawable.extra_milk),
                            contentDescription = "Extra Milk",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                color = colorLightGrey.copy(alpha = 0.3f),
                thickness = 1.dp
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Описание",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentCoffee?.description ?: coffee.description,
                    fontSize = 14.sp,
                    lineHeight = 24.sp,
                    color = colorLightGrey
                )

                val activeSellerId = coffee.sellerId?.takeIf { it > 0 }
                if (activeSellerId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                navController.navigate("${NavigationRoutes.SELLER_STORE}/$activeSellerId")
                            },
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Продавец",
                                    fontFamily = SoraFontFamily,
                                    fontSize = 11.sp,
                                    color = colorLightGrey
                                )
                                Text(
                                    coffee.sellerName ?: "Открыть магазин",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text("→", fontSize = 20.sp, color = colorDarkOrange)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Размер",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    availableSizes.forEach { size ->
                        SizeOption(
                            size = size.size,
                            volume = size.volume,
                            isSelected = selectedSize == size.size,
                            onClick = { viewModel.selectSize(size.size) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SizeOption(
    size: String,
    volume: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        Color.Transparent
    }

    val textColor = if (isSelected) {
        colorDarkOrange
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val borderColor = if (isSelected) {
        colorDarkOrange
    } else {
        colorLightGrey.copy(alpha = 0.5f)
    }

    Box(
        modifier = Modifier
            .height(if (volume != null) 56.dp else 44.dp)
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .background(backgroundColor)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = size,
                fontSize = 16.sp,
                fontWeight = FontWeight.W600,
                color = textColor
            )
            if (volume != null) {
                Text(
                    text = volume,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W400,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun BottomOrderPanel(
    price: String,
    isInCart: Boolean,
    onButtonClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Цена",
                    fontSize = 14.sp,
                    lineHeight = 16.8.sp,
                    fontWeight = FontWeight.W400,
                    color = colorLightGrey
                )
                Text(
                    text = price,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W600,
                    color = colorDarkOrange,
                    fontFamily = SoraFontFamily
                )
            }

            Box(
                modifier = Modifier
                    .height(48.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isInCart) MaterialTheme.colorScheme.primaryContainer else colorDarkOrange
                    )
                    .border(
                        width = if (isInCart) 1.dp else 0.dp,
                        color = if (isInCart) colorDarkOrange else Color.Transparent,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onButtonClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isInCart) "В корзине" else "Купить сейчас",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W600,
                    color = if (isInCart) colorDarkOrange else Color.White,
                    fontFamily = SoraFontFamily
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Coffee Detail Screen")
@Composable
fun CoffeeDetailScreenPreview() {
    CoffeeShopTheme {
        val mockCoffee = ProductResponse(
            id = 1,
            type = ProductCategoryResponse(1, "Cappuccino"),
            name = "Classic Cappuccino",
            description = "Traditional Italian coffee drink prepared with espresso, hot milk, and steamed milk foam. Perfect balance of coffee and milk with a rich, creamy texture.",
            sizes = listOf(
                ProductVariantResponse("S", 2.50f),
                ProductVariantResponse("M", 3.00f),
                ProductVariantResponse("L", 3.50f)
            ),
            imageName = "cappuccino.jpg"
        )

        CoffeeDetailScreen(
            navController = rememberNavController(),
            coffee = mockCoffee
        )
    }
}