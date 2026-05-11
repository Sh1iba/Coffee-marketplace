package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.example.coffeeshop.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.coffeeshop.data.remote.response.BranchResponse
import com.example.coffeeshop.data.remote.response.CartItemResponse
import com.example.coffeeshop.data.remote.response.ProductResponse
import com.example.coffeeshop.data.remote.response.ProductVariantResponse
import com.example.coffeeshop.data.remote.response.ProductCategoryResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorFoundationGrey
import com.example.coffeeshop.presentation.theme.colorLightGrey
import com.example.coffeeshop.presentation.theme.colorLightWhite
import com.example.coffeeshop.presentation.theme.colorSelectOrange
import com.example.coffeeshop.presentation.viewmodel.CartViewModel
import com.example.coffeeshop.presentation.viewmodel.CoffeeDetailViewModel
import com.example.coffeeshop.presentation.viewmodel.LocationState
import com.example.coffeeshop.presentation.viewmodel.LocationViewModel
import com.example.coffeeshop.presentation.viewmodel.OrderItem
import com.example.coffeeshop.presentation.viewmodel.OrderViewModel
import com.example.coffeeshop.presentation.viewmodel.ParsedAddress
import java.math.BigDecimal


data class OrderData(
    val items: List<CartItemResponse>,
    val address: String,
    val note: String,
    val totalPrice: Double,
    val deliveryFee: Double
)

@Composable
fun OrderScreen(
    navController: NavController,
    selectedItems: List<CartItemResponse> = emptyList(),
    totalPrice: Double = 0.0
) {
    val viewModel: OrderViewModel = hiltViewModel()
    val locationViewModel: LocationViewModel = hiltViewModel()

    val orderItems by viewModel.orderItems.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val addressNote by viewModel.addressNote.collectAsState()
    val showNoteDialog by viewModel.showNoteDialog.collectAsState()
    val navigateToOrders by viewModel.navigateToOrders.collectAsState()
    val branches by viewModel.branches.collectAsState()
    val selectedBranch by viewModel.selectedBranch.collectAsState()

    val locationState by locationViewModel.uiState.collectAsState()

    var selectedButton by remember { mutableStateOf("Доставка") }
    var showAddressConfirmationDialog by remember { mutableStateOf(false) }
    var showBranchPickerDialog by remember { mutableStateOf(false) }
    var tempOrderData by remember { mutableStateOf<OrderData?>(null) }

    LaunchedEffect(navigateToOrders) {
        if (navigateToOrders) {
            viewModel.resetAllNavigation()
            navController.navigate(NavigationRoutes.MY_ORDERS) {
                popUpTo(NavigationRoutes.HOME) { inclusive = false }
            }
        }
    }

    LaunchedEffect(selectedButton) {
        viewModel.setDeliveryType(if (selectedButton == "Доставка") "DELIVERY" else "PICKUP")
    }

    LaunchedEffect(locationState.selectedAddress) {
        if (locationState.selectedAddress.isNotEmpty()) {
            viewModel.loadAddressNote(locationState.selectedAddress)
            selectedButton = "Доставка"
        } else {
            viewModel.clearAddressNote("")
        }
    }

    LaunchedEffect(selectedItems) {
        if (selectedItems.isNotEmpty()) {
            viewModel.loadOrderItems(selectedItems)
        }
    }

    val scrollState = rememberScrollState()

    if (showAddressConfirmationDialog) {
        AddressConfirmationDialog(
            address = locationState.selectedAddress,
            onConfirm = {
                showAddressConfirmationDialog = false
                tempOrderData?.let { data ->
                    viewModel.createOrder(
                        items = data.items,
                        address = data.address,
                        note = data.note,
                        totalPrice = data.totalPrice,
                        deliveryFee = data.deliveryFee
                    )
                }
            },
            onEdit = {
                showAddressConfirmationDialog = false
                locationViewModel.onShowAddressDialogChange(true)
            },
            onDismiss = {
                showAddressConfirmationDialog = false
                tempOrderData = null
            }
        )
    }

    if (showBranchPickerDialog && branches.isNotEmpty()) {
        BranchPickerDialog(
            branches = branches,
            selected = selectedBranch,
            onSelect = { branch ->
                viewModel.selectBranch(branch)
                showBranchPickerDialog = false
            },
            onDismiss = { showBranchPickerDialog = false }
        )
    }

    if (locationState.showAddressDialog) {
        AddressSelectionDialog(
            currentAddress = locationState.selectedAddress,
            searchQuery = locationState.addressSearchQuery,
            onSearchQueryChange = { locationViewModel.onAddressSearchQueryChange(it) },
            searchResults = locationState.addressSearchResults,
            isLoading = locationState.isAddressLoading,
            onAddressSelected = { address ->
                val addressText = address.toString()
                locationViewModel.onAddressSelected(addressText)
                viewModel.clearAddressNote(locationState.selectedAddress)
            },
            onDismiss = { locationViewModel.onShowAddressDialogChange(false) },
            isTablet = false,
            screenHeight = LocalConfiguration.current.screenHeightDp.dp
        )
    }

    if (showNoteDialog) {
        AddressNoteDialog(
            currentNote = addressNote,
            onNoteChange = {
                if (locationState.selectedAddress.isNotEmpty()) {
                    viewModel.saveAddressNote(locationState.selectedAddress, it)
                }
            },
            onSave = { viewModel.hideNoteDialog() },
            onDismiss = { viewModel.hideNoteDialog() }
        )
    }

    Scaffold(
        topBar = {
            OrderTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomOrderPanel(
                walletBalance = "₽5.53",
                totalPrice = totalPrice,
                deliveryType = selectedButton,
                selectedItems = selectedItems,
                locationState = locationState,
                addressNote = addressNote,
                onOrderClick = { items, address, note, total, deliveryFee ->
                    if (selectedButton == "Доставка" && address.isNotEmpty()) {
                        tempOrderData = OrderData(items, address, note, total, deliveryFee)
                        showAddressConfirmationDialog = true
                    } else {
                        viewModel.createOrder(
                            items = items,
                            address = address,
                            note = note,
                            totalPrice = total,
                            deliveryFee = deliveryFee
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colorDarkOrange)
                    }
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Ошибка загрузки",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.clearError()
                                viewModel.loadOrderItems(selectedItems)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorDarkOrange
                            )
                        ) {
                            Text("Повторить")
                        }
                    }
                }
                else -> {
                    OrderContent(
                        orderItems = orderItems,
                        totalPrice = totalPrice,
                        locationState = locationState,
                        addressNote = addressNote,
                        parsedAddress = viewModel.parseAddress(locationState.selectedAddress),
                        selectedButton = selectedButton,
                        selectedBranch = selectedBranch,
                        hasBranches = branches.isNotEmpty(),
                        onLocationClick = { locationViewModel.onShowAddressDialogChange(true) },
                        onNoteClick = { viewModel.showNoteDialog() },
                        onSelectedButtonChange = { newButton -> selectedButton = newButton },
                        onBranchClick = { showBranchPickerDialog = true }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressConfirmationDialog(
    address: String,
    onConfirm: () -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Подтверждение адреса",
                fontWeight = FontWeight.W600,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Адрес доставки:",
                    fontSize = 14.sp,
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = address,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.W500,
                    color = colorScheme.outlineVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Всё верно?",
                    fontSize = 14.sp,
                    color = colorScheme.outlineVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorDarkOrange
                )
            ) {
                Text(
                    text = "Да, всё верно",
                    fontFamily = SoraFontFamily,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Row {
                OutlinedButton(
                    onClick = onEdit,
                    border = BorderStroke(1.dp, colorLightGrey)
                ) {
                    Text("Изменить адрес", color = MaterialTheme.colorScheme.onBackground)
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("Отмена", color = Color.Gray)
                }
            }
        }
    )
}

@Composable
fun OrderContent(
    orderItems: List<OrderItem>,
    totalPrice: Double,
    locationState: LocationState,
    addressNote: String,
    parsedAddress: ParsedAddress,
    selectedButton: String,
    selectedBranch: BranchResponse?,
    hasBranches: Boolean,
    onLocationClick: () -> Unit,
    onNoteClick: () -> Unit,
    onSelectedButtonChange: (String) -> Unit,
    onBranchClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 24.dp)
                .background(colorScheme.surface, shape = RoundedCornerShape(12))
                .fillMaxWidth()
                .height(43.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .background(
                        color = if (selectedButton == "Доставка") colorDarkOrange else colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectedButtonChange("Доставка") }
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = "Доставка",
                    fontFamily = SoraFontFamily,
                    fontWeight = if (selectedButton == "Доставка") FontWeight.W600 else FontWeight.W400,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = if (selectedButton == "Доставка") Color.White else colorScheme.outline,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .padding(all = 4.dp)
                    .background(
                        color = if (selectedButton == "Забрать") colorDarkOrange else colorScheme.surface,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable { onSelectedButtonChange("Забрать") }
                    .fillMaxHeight()
                    .weight(1f)
            ) {
                Text(
                    text = "Забрать",
                    fontFamily = SoraFontFamily,
                    fontWeight = if (selectedButton == "Забрать") FontWeight.W600 else FontWeight.W400,
                    fontSize = 16.sp,
                    lineHeight = 21.sp,
                    color = if (selectedButton == "Забрать") Color.White else colorScheme.outline,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }

        if (hasBranches) {
            BranchPickerSection(
                selectedBranch = selectedBranch,
                deliveryType = selectedButton,
                onBranchClick = onBranchClick,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 8.dp)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            if (selectedButton == "Доставка") {
                Text(
                    text = "Адрес Доставки",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Column {
                    Text(
                        text = if (parsedAddress.mainAddress.isNotEmpty()) parsedAddress.mainAddress else "Выберите адрес",
                        color = colorScheme.outline,
                        fontWeight = FontWeight.W600,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (parsedAddress.addressDetails.isNotEmpty()) {
                        Text(
                            text = parsedAddress.addressDetails,
                            color = colorScheme.outlineVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (addressNote.isNotEmpty()) {
                        Text(
                            text = "Примечание: $addressNote",
                            color = colorDarkOrange,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    OutlinedButton(
                        onClick = onLocationClick,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colorLightGrey),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Изменить Адрес",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    OutlinedButton(
                        onClick = onNoteClick,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, colorLightGrey),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        enabled = locationState.selectedAddress.isNotEmpty()
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.note),
                            contentDescription = null,
                            tint = if (locationState.selectedAddress.isNotEmpty()) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                Color.Gray
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (addressNote.isNotEmpty()) "Изм. примечание" else "Примечание",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W400,
                            fontSize = 14.sp,
                            color = if (locationState.selectedAddress.isNotEmpty()) {
                                MaterialTheme.colorScheme.onBackground
                            } else {
                                Color.Gray
                            }
                        )
                    }
                }

                Divider(
                    modifier = Modifier.padding(horizontal = 17.dp),
                    color = colorScheme.surfaceTint,
                    thickness = 1.dp
                )
            }

            Text(
                text = "Ваш заказ (${orderItems.size} товаров)",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            orderItems.forEach { orderItem ->
                OrderCoffeeCard(
                    coffeeName = orderItem.cartItem.name,
                    coffeeType = orderItem.coffeeData?.type?.type ?: "Кофе",
                    coffeeDescription = orderItem.cartItem.selectedSize,
                    imageUrl = orderItem.cartItem.imageUrl,
                    quantity = orderItem.cartItem.quantity
                )
            }
        }

        Divider(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            color = colorScheme.surfaceTint,
            thickness = 4.dp
        )

        val deliveryFee = if (selectedButton == "Доставка") 50.00 else 0.00
        val finalTotalPrice = totalPrice + deliveryFee

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Платежное Резюме",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Сумма заказа",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = Color.Gray
                )
                Text(
                    text = "₽${"%.2f".format(totalPrice)}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedButton == "Доставка") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Плата за доставку",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "₽${"%.2f".format(deliveryFee)}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Итого",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "₽${"%.2f".format(finalTotalPrice)}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    color = colorDarkOrange
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressNoteDialog(
    currentNote: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var noteText by remember { mutableStateOf(currentNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Примечание к адресу",
                fontWeight = FontWeight.W600,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Добавьте уточнения для курьера (подъезд, этаж, код домофона и т.д.)",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = "Например: 3 подъезд, 5 этаж, код 1234",
                            color = Color.LightGray
                        )
                    },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = colorDarkOrange,
                        unfocusedBorderColor = colorLightGrey
                    ),
                    singleLine = false,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onNoteChange(noteText)
                    onSave()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorDarkOrange
                ),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "Сохранить",
                    fontFamily = SoraFontFamily,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, colorLightGrey)
            ) {
                Text("Отмена", color = MaterialTheme.colorScheme.onBackground)
            }
        }
    )
}

@Composable
fun OrderTopBar(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    painter = painterResource(id = R.drawable.leftarrow),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                )
            }

            Text(
                text = "Оформление заказа",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Box(modifier = Modifier.size(44.dp))
        }
    }
}

@Composable
fun BottomOrderPanel(
    walletBalance: String,
    totalPrice: Double,
    deliveryType: String,
    selectedItems: List<CartItemResponse>,
    locationState: LocationState,
    addressNote: String,
    onOrderClick: (List<CartItemResponse>, String, String, Double, Double) -> Unit = { _, _, _, _, _ -> }
) {
    val deliveryFee = if (deliveryType == "Доставка") 50.00 else 0.00
    val finalTotalPrice = totalPrice + deliveryFee

    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.wallet),
                        contentDescription = null,
                        tint = colorDarkOrange,
                        modifier = Modifier.size(22.dp)
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Наличные/Кошелек",
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            color = colorScheme.outline
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.img),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.outline)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colorDarkOrange)
                    .clickable {
                        onOrderClick(
                            selectedItems,
                            locationState.selectedAddress,
                            addressNote,
                            finalTotalPrice,
                            deliveryFee
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Заказать за ₽${"%.2f".format(finalTotalPrice)}",
                    fontWeight = FontWeight.W600,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun OrderCoffeeCard(
    coffeeName: String,
    coffeeType: String,
    coffeeDescription: String,
    imageUrl: String,
    quantity: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(16.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = coffeeName,
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = coffeeName,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = coffeeType,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = "Размер: $coffeeDescription",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .wrapContentHeight(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$quantity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun BranchPickerSection(
    selectedBranch: BranchResponse?,
    deliveryType: String,
    onBranchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = if (deliveryType == "Доставка") "Откуда доставить" else "Куда приехать"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onBranchClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = SoraFontFamily,
                fontSize = 12.sp,
                color = colorLightGrey
            )
            Text(
                text = selectedBranch?.name ?: "Выберите филиал",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 14.sp,
                color = if (selectedBranch != null) MaterialTheme.colorScheme.onBackground else colorDarkOrange
            )
            if (selectedBranch != null) {
                Text(
                    text = selectedBranch.address,
                    fontFamily = SoraFontFamily,
                    fontSize = 12.sp,
                    color = colorLightGrey,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            painter = painterResource(id = R.drawable.edit),
            contentDescription = null,
            tint = colorDarkOrange,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun BranchPickerDialog(
    branches: List<BranchResponse>,
    selected: BranchResponse?,
    onSelect: (BranchResponse) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выберите филиал",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                branches.forEach { branch ->
                    val isSelected = branch.id == selected?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) colorDarkOrange.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                            .clickable { onSelect(branch) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = branch.name,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = if (isSelected) colorDarkOrange else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = branch.address,
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = colorLightGrey,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (branch.workingHours != null) {
                                Text(
                                    text = branch.workingHours,
                                    fontFamily = SoraFontFamily,
                                    fontSize = 11.sp,
                                    color = colorDarkOrange
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = colorDarkOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = colorLightGrey)
            }
        }
    )
}

@Preview(showBackground = true, showSystemUi = true, name = "OrderScreenPreview")
@Composable
fun OrderScreenPreview(){
    val mockItems = listOf(
        CartItemResponse(
            id = 1,
            name = "Cappuccino с очень длинным названием которое может сдвигать элементы",
            selectedSize = "M",
            price = 3.00f,
            quantity = 2,
            totalPrice = 6.00f,
            imageUrl = ""
        ),
        CartItemResponse(
            id = 2,
            name = "Latte",
            selectedSize = "L",
            price = 4.50f,
            quantity = 1,
            totalPrice = 4.50f,
            imageUrl = ""
        )
    )

    OrderScreen(
        navController = rememberNavController(),
        totalPrice = 10.50
    )
}