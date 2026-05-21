package com.example.coffeeshop.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.coffeeshop.data.remote.response.OrderItemResponse
import com.example.coffeeshop.data.remote.response.OrderResponse
import com.example.coffeeshop.presentation.theme.*
import com.example.coffeeshop.presentation.viewmodel.OrderHistoryViewModel
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val DONE_STATUSES = setOf("DELIVERED", "CANCELLED")

private fun parseDate(raw: String): String = try {
    val dt = LocalDateTime.parse(raw)
    dt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm"))
} catch (_: Exception) { raw }

private fun statusLabel(s: String) = when (s) {
    "PENDING"          -> "Ожидает подтверждения"
    "CONFIRMED"        -> "Подтверждён"
    "COOKING"          -> "Готовится"
    "READY_FOR_PICKUP" -> "Готов к выдаче"
    "DELIVERED"        -> "Доставлен"
    "CANCELLED"        -> "Отменён"
    else               -> s
}

private fun statusColor(s: String) = when (s) {
    "CONFIRMED"        -> Color(0xFF1E88E5)
    "COOKING"          -> Color(0xFF8E24AA)
    "READY_FOR_PICKUP" -> Color(0xFF43A047)
    "DELIVERED"        -> Color(0xFF43A047)
    "CANCELLED"        -> Color(0xFFE53935)
    else               -> Color(0xFFF57C00)
}

// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(navController: NavController) {
    val viewModel: OrderHistoryViewModel = hiltViewModel()
    val orders          by viewModel.orders.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val error           by viewModel.error.collectAsState()
    val reviewedOrderIds by viewModel.reviewedOrderIds.collectAsState()
    val reviewError     by viewModel.reviewError.collectAsState()

    var reviewingOrderId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) { viewModel.loadOrderHistory() }

    reviewingOrderId?.let { orderId ->
        ReviewDialog(
            onDismiss = { reviewingOrderId = null },
            onSubmit = { rating, comment ->
                viewModel.submitReview(orderId, rating, comment.ifBlank { null })
                reviewingOrderId = null
            }
        )
    }

    val doneOrders = orders.filter { it.status in DONE_STATUSES }
    val currency   = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 8.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        "История заказов",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        error?.let { msg ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK", color = colorDarkOrange) } }
            ) { Text(msg) }
        }

        reviewError?.let { msg ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = { TextButton(onClick = { viewModel.clearReviewError() }) { Text("OK", color = colorDarkOrange) } }
            ) { Text(msg) }
        }

        when {
            isLoading && orders.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = colorDarkOrange) }

            doneOrders.isEmpty() -> EmptyHistoryState(
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 20.dp, end = 20.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(doneOrders, key = { it.id }) { order ->
                    HistoryOrderCard(
                        order = order,
                        currency = currency,
                        isReviewed = order.id in reviewedOrderIds,
                        onReviewClick = { reviewingOrderId = order.id }
                    )
                }
            }
        }
    }
}

// ─── Status chip ──────────────────────────────────────────────────────────────

@Composable
private fun StatusChip(status: String) {
    val color = statusColor(status)
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Text(
            statusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 11.sp,
            color = color
        )
    }
}

// ─── Compact item row ─────────────────────────────────────────────────────────

@Composable
private fun CompactItemRow(item: OrderItemResponse, currency: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${item.coffeeName} · ${item.selectedSize}",
            fontFamily = SoraFontFamily,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "× ${item.quantity}",
                fontFamily = SoraFontFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                currency.format(item.totalPrice),
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ─── History order card ───────────────────────────────────────────────────────

@Composable
private fun HistoryOrderCard(
    order: OrderResponse,
    currency: NumberFormat,
    isReviewed: Boolean = false,
    onReviewClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Заказ #${order.id}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        parseDate(order.orderDate),
                        fontFamily = SoraFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = order.status)
            }

            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                order.items.forEach { item -> CompactItemRow(item = item, currency = currency) }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Итого: ${currency.format(order.totalAmount)}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (expanded) "Свернуть" else "Детали",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = colorDarkOrange
                    )
                }
            }

            if (order.status == "DELIVERED" && !isReviewed) {
                TextButton(
                    onClick = onReviewClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = colorDarkOrange)
                    Spacer(Modifier.width(6.dp))
                    Text("Оценить заказ", fontFamily = SoraFontFamily, fontSize = 13.sp, color = colorDarkOrange)
                }
            }
        }
    }
}

// ─── Review dialog ────────────────────────────────────────────────────────────

@Composable
private fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (rating: Int, comment: String) -> Unit) {
    var rating  by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Оценить заказ", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Выберите оценку:", fontFamily = SoraFontFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "$i звезд",
                                tint = if (i <= rating) colorDarkOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    placeholder = { Text("Комментарий (необязательно)", fontFamily = SoraFontFamily, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDarkOrange,
                        focusedLabelColor = colorDarkOrange
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (rating > 0) onSubmit(rating, comment) },
                enabled = rating > 0,
                colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
            ) {
                Text("Отправить", fontFamily = SoraFontFamily)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", fontFamily = SoraFontFamily, color = colorDarkOrange)
            }
        }
    )
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyHistoryState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(colorDarkOrange.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(44.dp), tint = colorDarkOrange)
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "История пуста",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Здесь появятся завершённые и отменённые заказы",
            fontFamily = SoraFontFamily,
            fontSize = 14.sp,
            color = colorLightGrey,
            textAlign = TextAlign.Center
        )
    }
}

// ─── Keep old public composables used in older screens ───────────────────────

@Composable
fun OrderCard(order: OrderResponse) {
    val currency = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    HistoryOrderCard(order = order, currency = currency, isReviewed = false, onReviewClick = {})
}

@Composable
fun OrderItemRow(item: OrderItemResponse) {
    val currency = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    CompactItemRow(item = item, currency = currency)
}

@Composable
fun EmptyOrderHistoryState(navController: NavController, onNavigateToCatalog: () -> Unit = { navController.popBackStack() }) {
    EmptyHistoryState()
}
