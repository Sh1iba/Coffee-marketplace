@file:Suppress("UNCHECKED_CAST")

package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.twotone.ArrowBack
import androidx.compose.material.icons.twotone.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import java.net.URLEncoder
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
import com.example.coffeeshop.R
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.SellerResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorGrey
import com.example.coffeeshop.presentation.theme.colorGreyWhite
import com.example.coffeeshop.presentation.viewmodel.CartViewModel
import com.example.coffeeshop.presentation.viewmodel.HomeViewModel
import com.example.coffeeshop.presentation.viewmodel.LocationState
import com.example.coffeeshop.presentation.viewmodel.LocationViewModel
import com.example.coffeeshop.data.managers.SavedAddress
import com.example.coffeeshop.presentation.viewmodel.SearchViewModel


@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    val viewModel: HomeViewModel = hiltViewModel()
    val cartViewModel: CartViewModel = hiltViewModel()
    val searchViewModel: SearchViewModel = hiltViewModel()
    val locationViewModel: LocationViewModel = hiltViewModel()

    val homeState by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val sellers by viewModel.sellers.collectAsState()
    val showSizeDialog by viewModel.showSizeDialog.collectAsState()
    val searchState by searchViewModel.uiState.collectAsState()
    val locationState by locationViewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.loadCoffeeData()
        cartViewModel.loadCart()
    }
    LaunchedEffect(searchState.searchText) {
        viewModel.searchCoffee(searchState.searchText)
    }

    val popularProducts by viewModel.popularProducts.collectAsState()
    val recommendedProducts by viewModel.recommendedProducts.collectAsState()
    val showSections = !homeState.isSearching && homeState.selectedTypeName == "Все товары"

    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(modifier = Modifier.fillMaxSize(), state = listState) {

            // ── Шапка ─────────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF313131), Color(0xFF111111)),
                                start = Offset(0f, Float.POSITIVE_INFINITY),
                                end = Offset(Float.POSITIVE_INFINITY, 0f)
                            ),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                        .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                        .padding(top = 56.dp, start = 24.dp, end = 24.dp, bottom = 28.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (locationState.savedAddresses.isEmpty()) {
                                        locationViewModel.onShowAddressDialogChange(true)
                                    } else {
                                        locationViewModel.onShowMyAddressesDialogChange(true)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colorDarkOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Локация", fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorGrey)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = locationState.selectedAddress,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.W600,
                                        fontSize = 14.sp,
                                        color = colorGreyWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Image(painterResource(R.drawable.img), contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                                    .border(
                                        1.dp,
                                        if (searchState.isSearchFocused) colorDarkOrange else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(painterResource(R.drawable.search), contentDescription = null, modifier = Modifier.size(18.dp))
                                    BasicTextField(
                                        value = searchState.searchText,
                                        onValueChange = searchViewModel::onSearchTextChange,
                                        textStyle = TextStyle(fontSize = 16.sp, color = Color.White, fontFamily = SoraFontFamily),
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp)
                                            .onFocusChanged { searchViewModel.onSearchFocusChange(it.isFocused) },
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(onSearch = {
                                            searchViewModel.performSearch(viewModel::searchCoffee)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }),
                                        decorationBox = { innerTextField ->
                                            Box {
                                                if (searchState.searchText.isEmpty()) {
                                                    Text("Поиск товаров...", fontSize = 16.sp, color = Color.Gray, fontFamily = SoraFontFamily)
                                                }
                                                innerTextField()
                                            }
                                        }
                                    )
                                    if (searchState.searchText.isNotEmpty()) {
                                        IconButton(
                                            onClick = {
                                                searchViewModel.clearSearch()
                                                viewModel.searchCoffee("")
                                                keyboardController?.hide()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, null, tint = Color.Gray)
                                        }
                                    }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(colorDarkOrange, RoundedCornerShape(12.dp))
                                    .clickable {
                                        searchViewModel.performSearch(viewModel::searchCoffee)
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(painterResource(R.drawable.search), contentDescription = null, modifier = Modifier.size(24.dp), colorFilter = ColorFilter.tint(Color.White))
                            }
                        }
                    }
                }
            }

            // ── История поиска ─────────────────────────────────────────────────
            if (searchState.isSearchFocused && searchState.searchText.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF2C2C2C),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)
                    ) {
                        Column {
                            if (searchState.searchHistory.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { searchViewModel.clearSearchHistory() }.padding(8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text("Очистить историю", color = Color.Gray, fontSize = 14.sp, fontFamily = SoraFontFamily)
                                }
                                searchState.searchHistory.forEachIndexed { index, item ->
                                    Text(
                                        text = item,
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            searchViewModel.selectSearchHistoryItem(item, viewModel::searchCoffee)
                                            keyboardController?.hide()
                                            focusManager.clearFocus()
                                        }.padding(12.dp),
                                        color = Color.White,
                                        fontFamily = SoraFontFamily,
                                        fontSize = 16.sp
                                    )
                                    if (index < searchState.searchHistory.lastIndex) {
                                        HorizontalDivider(color = Color(0xFF3A3A3A))
                                    }
                                }
                            } else {
                                Text("Нет истории поиска", modifier = Modifier.padding(12.dp), color = Color.Gray, fontFamily = SoraFontFamily, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // ── Категории ──────────────────────────────────────────────────────
            if (homeState.productTypes.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        items(homeState.productTypes) { category ->
                            val isSelected = category == homeState.selectedTypeName
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) colorDarkOrange else MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp,
                                modifier = Modifier.clickable { viewModel.onCoffeeTypeSelected(category) }
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    fontFamily = SoraFontFamily,
                                    fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W400,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // ── Магазины ───────────────────────────────────────────────────────
            if (showSections && sellers.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Text(
                            text = "Магазины",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(sellers) { seller ->
                                SellerChipCard(seller = seller, onClick = {
                                    navController.navigate("${NavigationRoutes.SELLER_STORE}/${seller.id}")
                                })
                            }
                        }
                    }
                }
            }

            // ── Популярное ────────────────────────────────────────────────────
            if (showSections && popularProducts.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                        Text(
                            text = "Популярное",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(popularProducts) { product ->
                                PopularProductCard(
                                    product = product,
                                    viewModel = viewModel,
                                    navController = navController,
                                    cartViewModel = cartViewModel
                                )
                            }
                        }
                    }
                }
            }

            // ── Рекомендации ──────────────────────────────────────────────────
            if (showSections && recommendedProducts.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)) {
                        Text(
                            text = "Рекомендуем",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommendedProducts) { product ->
                                PopularProductCard(
                                    product = product,
                                    viewModel = viewModel,
                                    navController = navController,
                                    cartViewModel = cartViewModel
                                )
                            }
                        }
                    }
                }
            }

            // ── Заголовок раздела товаров ──────────────────────────────────────
            item {
                Text(
                    text = if (showSections) "Все товары" else homeState.selectedTypeName,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
                )
            }

            // ── Список товаров ─────────────────────────────────────────────────
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorDarkOrange)
                    }
                }
            } else if (error != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(error ?: "", color = MaterialTheme.colorScheme.onBackground, fontFamily = SoraFontFamily, fontSize = 14.sp)
                        TextButton(onClick = { viewModel.loadCoffeeData() }) {
                            Text("Повторить", color = colorDarkOrange, fontFamily = SoraFontFamily)
                        }
                    }
                }
            } else if (homeState.filteredProducts.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("Товары не найдены", color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = SoraFontFamily)
                    }
                }
            } else {
                items(homeState.filteredProducts.chunked(2)) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        pair.forEach { coffee ->
                            ProductCard(
                                coffee = coffee,
                                viewModel = viewModel,
                                navController = navController,
                                cartViewModel = cartViewModel,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        // ── Диалог выбора размера ──────────────────────────────────────────────
        showSizeDialog?.let { coffee ->
            SizeSelectionDialog(
                coffee = coffee,
                onDismiss = { viewModel.hideSizeSelectionDialog() },
                onSizeSelected = { selectedSize ->
                    cartViewModel.addToCart(coffee.id, selectedSize)
                }
            )
        }

    }

    if (locationState.showMyAddressesDialog) {
        SavedAddressesDialog(
            savedAddresses = locationState.savedAddresses,
            onSelect = locationViewModel::confirmSavedAddress,
            onDelete = locationViewModel::deleteSavedAddress,
            onEditAddress = locationViewModel::startEditingSavedAddress,
            onAddNew = {
                locationViewModel.onShowMyAddressesDialogChange(false)
                locationViewModel.onShowAddressDialogChange(true)
            },
            onDismiss = { locationViewModel.onShowMyAddressesDialogChange(false) }
        )
    }

    if (locationState.showAddressDialog) {
        YandexLocationPickerDialog(
            state = locationState,
            onSearchQueryChange = locationViewModel::onAddressSearchQueryChange,
            onCameraPositionChanged = locationViewModel::onCameraPositionChanged,
            onApartmentChange = locationViewModel::onApartmentChange,
            onEntranceChange = locationViewModel::onEntranceChange,
            onFloorChange = locationViewModel::onFloorChange,
            onIntercomChange = locationViewModel::onIntercomChange,
            onCourierCommentChange = locationViewModel::onCourierCommentChange,
            onConfirm = locationViewModel::confirmLocation,
            onDismiss = { locationViewModel.onShowAddressDialogChange(false) }
        )
    }
}

@Composable
fun PopularProductCard(
    product: ProductResponse,
    viewModel: HomeViewModel,
    navController: NavController,
    cartViewModel: CartViewModel
) {
    val defaultPrice = remember(product) { viewModel.getDefaultPrice(product) }

    val isInCart by remember(cartViewModel.cartItems, product.id) {
        derivedStateOf { cartViewModel.cartItems.value.any { it.id == product.id } }
    }

    Surface(
        modifier = Modifier
            .width(150.dp)
            .clickable {
                val sizesEncoded = viewModel.encodeSizesForNavigation(product)
                val imageUrlEncoded = URLEncoder.encode(product.imageUrl, "UTF-8")
                navController.navigate(
                    "${NavigationRoutes.DETAIL}/${product.id}/${product.name}/${product.type.type}/${product.description}?imageUrl=$imageUrlEncoded&sizes=$sizesEncoded&favoriteSize=&sellerId=${product.sellerId ?: -1L}"
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                product.name,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                product.type.type,
                fontFamily = SoraFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "от ₽${defaultPrice.toInt()}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            if (isInCart) Color.White else colorDarkOrange,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            if (isInCart) 1.5.dp else 0.dp,
                            if (isInCart) colorDarkOrange else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { viewModel.showSizeSelectionDialog(product) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isInCart) {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(14.dp), tint = colorDarkOrange)
                    } else {
                        Text("+", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Normal)
                    }
                }
            }
        }
    }
}


@Composable
fun SavedAddressesDialog(
    savedAddresses: List<SavedAddress>,
    onSelect: (SavedAddress) -> Unit,
    onDelete: (String) -> Unit,
    onEditAddress: (SavedAddress) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Мои адреса",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(4.dp))
                savedAddresses.forEachIndexed { index, addr ->
                    if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(addr) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, null, tint = colorDarkOrange, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                addr.label,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                addr.address,
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { onEditAddress(addr) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Изменить адрес",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onDelete(addr.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onAddNew,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Добавить новый адрес", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun YandexLocationPickerDialog(
    state: LocationState,
    onSearchQueryChange: (String) -> Unit,
    onCameraPositionChanged: (Double, Double) -> Unit,
    onApartmentChange: (String) -> Unit,
    onEntranceChange: (String) -> Unit,
    onFloorChange: (String) -> Unit,
    onIntercomChange: (String) -> Unit,
    onCourierCommentChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // ── Шапка ─────────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.TwoTone.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Выберите адрес доставки",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // ── Поиск ──────────────────────────────────────────────────
                OutlinedTextField(
                    value = state.addressSearchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Найти адрес...", fontFamily = SoraFontFamily) },
                    leadingIcon = {
                        if (state.isAddressLoading)
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = colorDarkOrange)
                        else
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    trailingIcon = {
                        if (state.addressSearchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDarkOrange,
                        focusedLabelColor = colorDarkOrange
                    )
                )

                if (state.searchError.isNotBlank()) {
                    Text(
                        text = state.searchError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontFamily = SoraFontFamily,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // ── Карта ──────────────────────────────────────────────────
                HomeYandexMapPicker(
                    initialLat = state.lat,
                    initialLon = state.lon,
                    targetPoint = state.targetPoint,
                    onCameraIdle = onCameraPositionChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // ── Нижняя панель (Delivery Club стиль) ────────────────────
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Обнаруженный адрес
                        if (state.detectedAddress.isNotBlank()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.TwoTone.Place,
                                    contentDescription = null,
                                    tint = colorDarkOrange,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Text(
                                    text = state.detectedAddress,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                        }

                        // Квартира / Подъезд / Этаж
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AddressSmallField(
                                value = state.apartment,
                                onValueChange = onApartmentChange,
                                label = "Квартира",
                                modifier = Modifier.weight(1.2f)
                            )
                            AddressSmallField(
                                value = state.entrance,
                                onValueChange = onEntranceChange,
                                label = "Подъезд",
                                modifier = Modifier.weight(1f)
                            )
                            AddressSmallField(
                                value = state.floor,
                                onValueChange = onFloorChange,
                                label = "Этаж",
                                modifier = Modifier.weight(0.8f)
                            )
                        }

                        // Домофон
                        OutlinedTextField(
                            value = state.intercom,
                            onValueChange = onIntercomChange,
                            label = { Text("Домофон", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorDarkOrange,
                                focusedLabelColor = colorDarkOrange
                            )
                        )

                        // Комментарий к курьеру
                        OutlinedTextField(
                            value = state.courierComment,
                            onValueChange = onCourierCommentChange,
                            label = { Text("Комментарий к курьеру", fontFamily = SoraFontFamily, fontSize = 12.sp) },
                            placeholder = { Text("Например: оставить у двери", fontFamily = SoraFontFamily, fontSize = 13.sp, color = Color.Gray) },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colorDarkOrange,
                                focusedLabelColor = colorDarkOrange
                            )
                        )

                        // Кнопка подтверждения
                        Button(
                            onClick = onConfirm,
                            enabled = state.detectedAddress.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorDarkOrange,
                                disabledContainerColor = colorDarkOrange.copy(alpha = 0.35f)
                            )
                        ) {
                            Text(
                                text = "Подтвердить адрес",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressSmallField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontFamily = SoraFontFamily, fontSize = 11.sp) },
        singleLine = true,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorDarkOrange,
            focusedLabelColor = colorDarkOrange
        )
    )
}

@Composable
private fun HomeYandexMapPicker(
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
                            if (finished) {
                                onCameraIdleLatest.value(pos.target.latitude, pos.target.longitude)
                            }
                        }
                    })
                }
            },
            update = { view ->
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
fun SellersRow(
    sellers: List<SellerResponse>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    Column(modifier = modifier) {
        Text(
            text = "Магазины",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = if (isTablet) 18.sp else 16.sp,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(end = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sellers) { seller ->
                SellerChipCard(seller = seller, onClick = {
                    navController.navigate("${NavigationRoutes.SELLER_STORE}/${seller.id}")
                })
            }
        }
    }
}

@Composable
fun SellerChipCard(seller: SellerResponse, onClick: () -> Unit) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    Surface(
        modifier = Modifier
            .width(if (isTablet) 180.dp else 150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3A3A3A), Color(0xFF1A1A1A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!seller.logoImage.isNullOrEmpty()) {
                    AsyncImage(
                        model = seller.logoImage,
                        contentDescription = seller.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = seller.name,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = if (isTablet) 14.sp else 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("★", fontSize = 10.sp, color = colorDarkOrange)
                Text(
                    text = "%.1f".format(seller.rating),
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "· ${seller.category}",
                    fontFamily = SoraFontFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun CoffeeCategoryColumn(
    productList: List<ProductResponse>,
    sellers: List<SellerResponse> = emptyList(),
    viewModel: HomeViewModel,
    navController: NavController,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    val isTablet = LocalConfiguration.current.screenWidthDp >= 600
    val rowBottomPadding = if (isTablet) 24.dp else 16.dp

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        if (sellers.isNotEmpty()) {
            item {
                SellersRow(
                    sellers = sellers,
                    navController = navController,
                    modifier = Modifier.padding(bottom = rowBottomPadding)
                )
            }
        }

        items(productList.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                pair.forEach { coffee ->
                    ProductCard(
                        coffee = coffee,
                        viewModel = viewModel,
                        navController = navController,
                        cartViewModel = cartViewModel,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(250.dp))
        }
    }
}

@Composable
fun ProductCard(
    coffee: ProductResponse,
    viewModel: HomeViewModel,
    navController: NavController,
    cartViewModel: CartViewModel,
    modifier: Modifier = Modifier
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val itemHeight = (screenHeight * 0.30f).coerceIn(200.dp, 260.dp)
    val imageHeight = (itemHeight * 0.52f).coerceIn(100.dp, 135.dp)
    val defaultPrice = remember(coffee) {
        viewModel.getDefaultPrice(coffee)
    }

    val isInCart by remember(cartViewModel.cartItems, coffee.id) {
        derivedStateOf {
            cartViewModel.cartItems.value.any { it.id == coffee.id }
        }
    }

    Surface(
        modifier = modifier
            .height(itemHeight)
            .clickable {
                val sizesEncoded = viewModel.encodeSizesForNavigation(coffee)
                val imageUrlEncoded = URLEncoder.encode(coffee.imageUrl, "UTF-8")
                navController.navigate(
                    "${NavigationRoutes.DETAIL}/" +
                            "${coffee.id}/" +
                            "${coffee.name}/" +
                            "${coffee.type.type}/" +
                            "${coffee.description}" +
                            "?imageUrl=$imageUrlEncoded" +
                            "&sizes=$sizesEncoded" +
                            "&favoriteSize=" +
                            "&sellerId=${coffee.sellerId ?: -1L}"
                )
            },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = coffee.imageUrl,
                    contentDescription = coffee.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = coffee.name,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = coffee.type.type,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outlineVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₽${"%.2f".format(defaultPrice)}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = if (isInCart) Color.White else colorDarkOrange,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = if (isInCart) 2.dp else 0.dp,
                            color = if (isInCart) colorDarkOrange else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            viewModel.showSizeSelectionDialog(coffee)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isInCart) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = "В корзине",
                            modifier = Modifier.size(18.dp),
                            tint = colorDarkOrange
                        )
                    } else {
                        Text(
                            text = "+",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SizeSelectionDialog(
    coffee: ProductResponse,
    onDismiss: () -> Unit,
    onSizeSelected: (String) -> Unit
) {
    var selectedSize by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите размер",
                fontWeight = FontWeight.W600,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = coffee.name,
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                coffee.sizes.forEach { size ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedSize = size.size },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSize == size.size,
                            onClick = { selectedSize = size.size },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorDarkOrange
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Размер ${size.size}",
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "₽${"%.2f".format(size.price)}",
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            color = colorDarkOrange
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedSize?.let { onSizeSelected(it) }
                    onDismiss()
                },
                enabled = selectedSize != null
            ) {
                Text(
                    text = "Добавить в корзину",
                    fontWeight = FontWeight.W600,
                    color = if (selectedSize != null) colorDarkOrange else Color.Gray
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Отмена",
                    fontWeight = FontWeight.W500,
                    color = Color.Gray
                )
            }
        }
    )
}

data class BottomMenuItem(
    val label: String,
    val icon: Int,
    val selectedIndicator: Int,
    val route: String
)

@Composable
fun BottomMenuIcon(item: BottomMenuItem, isSelected: Boolean, onClick: () -> Unit) {
    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val iconSize = if (isTablet) 28.dp else 24.dp
    val indicatorSize = if (isTablet) 10.dp else 8.dp
    val horizontalPadding = if (isTablet) 12.dp else 8.dp
    val topPadding = if (isTablet) 6.dp else 4.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = item.icon),
            contentDescription = item.label,
            modifier = Modifier.size(iconSize),
            colorFilter = if (isSelected) {
                ColorFilter.tint(Color(0xFFC67C4E))
            } else {
                ColorFilter.tint(Color(0xFFA2A2A2))
            }
        )

        if (isSelected) {
            Image(
                painter = painterResource(id = item.selectedIndicator),
                contentDescription = "Selected indicator",
                modifier = Modifier
                    .size(indicatorSize)
                    .padding(top = topPadding)
            )
        }
    }
}

@Composable
fun BottomMenu(navController: NavController) {
    val menuItems = listOf(
        BottomMenuItem("Home", R.drawable.menu_home, R.drawable.selected_dot, NavigationRoutes.HOME),
        BottomMenuItem("Favorite", R.drawable.heart, R.drawable.selected_dot, NavigationRoutes.FAVORITE),
        BottomMenuItem("Cart", R.drawable.cart, R.drawable.selected_dot, NavigationRoutes.CART),
        BottomMenuItem("Orders", R.drawable.note, R.drawable.selected_dot, NavigationRoutes.MY_ORDERS),
        BottomMenuItem("Settings", R.drawable.settings_foreground, R.drawable.selected_dot, NavigationRoutes.SETTINGS)
    )

    val currentRoute =
        navController.currentBackStackEntryAsState().value?.destination?.route

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val height = if (isTablet) 110.dp else 99.dp
    val horizontalPadding = if (isTablet) 48.dp else 24.dp
    val verticalPadding = if (isTablet) 20.dp else 24.dp
    val cornerRadius = if (isTablet) 28.dp else 24.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius
        ),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = horizontalPadding,
                    vertical = verticalPadding
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            menuItems.forEach { item ->
                BottomMenuIcon(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, name = "pre")
@Composable
fun HomeScreenPreview() {
    HomeScreen(navController = rememberNavController())
}
