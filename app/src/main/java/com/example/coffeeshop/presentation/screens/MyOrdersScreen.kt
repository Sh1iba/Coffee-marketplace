package com.example.coffeeshop.presentation.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.coffeeshop.R
import com.example.coffeeshop.data.remote.response.OrderItemResponse
import com.example.coffeeshop.data.remote.response.OrderResponse
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.*
import com.example.coffeeshop.presentation.viewmodel.OrderHistoryViewModel
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

private val ACTIVE = setOf("PENDING", "CONFIRMED", "PROCESSING", "READY")

private fun fmtDate(raw: String) = try {
    LocalDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd.MM, HH:mm"))
} catch (_: Exception) { raw }

private fun activeStatusLabel(s: String) = when (s) {
    "PENDING"    -> "Ожидает подтверждения"
    "CONFIRMED"  -> "Подтверждён"
    "PROCESSING" -> "Готовится"
    "READY"      -> "Готов к выдаче!"
    else         -> s
}

private fun activeStatusColor(s: String) = when (s) {
    "PENDING"    -> Color(0xFFF57C00)
    "CONFIRMED"  -> Color(0xFF1E88E5)
    "PROCESSING" -> Color(0xFF8E24AA)
    "READY"      -> Color(0xFF43A047)
    else         -> Color(0xFFF57C00)
}

private val STEPS = listOf("Принят", "Готовится", "Готов", "Доставлен")

private fun currentStep(status: String) = when (status) {
    "CONFIRMED"  -> 0
    "PROCESSING" -> 1
    "READY"      -> 2
    "DELIVERED"  -> 3
    else         -> -1  // PENDING — ещё не подтверждён
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MyOrdersScreen(navController: NavController) {
    val viewModel: OrderHistoryViewModel = hiltViewModel()
    val allOrders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadOrderHistory() }

    val activeOrders = allOrders.filter { it.status in ACTIVE }
    val currency     = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Мои заказы",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (activeOrders.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(colorDarkOrange))
                            Text("live", fontFamily = SoraFontFamily, fontSize = 11.sp, color = colorDarkOrange, fontWeight = FontWeight.W600)
                        }
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        error?.let { msg ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.BottomCenter) {
                Snackbar(modifier = Modifier.padding(16.dp),
                    action = { TextButton(onClick = { viewModel.clearError() }) { Text("OK", color = colorDarkOrange) } }
                ) { Text(msg) }
            }
        }

        when {
            isLoading && allOrders.isEmpty() -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colorDarkOrange)
                }
            }

            activeOrders.isEmpty() -> {
                MyOrdersEmptyState(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onBrowse = {
                        navController.navigate(NavigationRoutes.HOME) {
                            popUpTo(NavigationRoutes.HOME) { inclusive = false }
                        }
                    }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 20.dp, end = 20.dp,
                        top = innerPadding.calculateTopPadding() + 12.dp,
                        bottom = innerPadding.calculateBottomPadding() + 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(activeOrders, key = { it.id }) { order ->
                        ActiveOrderTrackingCard(
                            order    = order,
                            currency = currency,
                            onCancel = { viewModel.cancelOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun MyOrdersEmptyState(modifier: Modifier = Modifier, onBrowse: () -> Unit) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.coffee_mascot),
            contentDescription = null,
            modifier = Modifier.size(140.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Нет активных заказов",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Здесь будут отображаться ваши\nактивные заказы в реальном времени",
            fontFamily = SoraFontFamily,
            fontSize = 14.sp,
            color = colorLightGrey,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBrowse,
            modifier = Modifier.height(50.dp).fillMaxWidth(0.65f),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
        ) {
            Text("Заказать кофе", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp, color = Color.White)
        }
    }
}

// ─── Active order tracking card ───────────────────────────────────────────────

@Composable
private fun ActiveOrderTrackingCard(
    order: OrderResponse,
    currency: NumberFormat,
    onCancel: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    var expanded         by remember { mutableStateOf(true) }

    val color = activeStatusColor(order.status)

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(18.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Заказ #${order.id}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        fmtDate(order.orderDate),
                        fontFamily = SoraFontFamily,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(20.dp), color = color.copy(alpha = 0.13f)) {
                    Text(
                        activeStatusLabel(order.status),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 11.sp,
                        color = color
                    )
                }
            }

            // ── Step progress ────────────────────────────────────────────────
            StepProgress(status = order.status)

            // ── Address ──────────────────────────────────────────────────────
            if (!order.deliveryAddress.isNullOrBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = colorLightGrey, modifier = Modifier.size(13.dp))
                    Text(
                        order.deliveryAddress ?: "",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // ── Items ─────────────────────────────────────────────────────────
            val visible = if (expanded) order.items else order.items.take(1)
            visible.forEach { item -> OrderLineItem(item, currency) }
            if (order.items.size > 1) {
                TextButton(onClick = { expanded = !expanded }, contentPadding = PaddingValues(0.dp)) {
                    Text(
                        if (expanded) "Свернуть" else "+ ещё ${order.items.size - 1}",
                        fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // ── Footer ────────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (order.deliveryFee > BigDecimal.ZERO) {
                        Text(
                            "Доставка: ${currency.format(order.deliveryFee)}",
                            fontFamily = SoraFontFamily, fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "Итого: ${currency.format(order.totalAmount)}",
                        fontFamily = SoraFontFamily, fontWeight = FontWeight.W700,
                        fontSize = 15.sp, color = colorDarkOrange
                    )
                }
                if (order.status == "PENDING") {
                    OutlinedButton(
                        onClick = { showCancelDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text("Отменить", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Отменить заказ?", fontFamily = SoraFontFamily, fontWeight = FontWeight.W600) },
            text  = { Text("Заказ #${order.id} будет отменён. Это действие необратимо.", fontFamily = SoraFontFamily) },
            confirmButton = {
                TextButton(onClick = { onCancel(); showCancelDialog = false }) {
                    Text("Да, отменить", color = MaterialTheme.colorScheme.error, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Назад", fontFamily = SoraFontFamily)
                }
            }
        )
    }
}

// ─── Step progress bar ────────────────────────────────────────────────────────

@Composable
private fun StepProgress(status: String) {
    val current = currentStep(status)

    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            STEPS.forEachIndexed { i, _ ->
                val done    = i <= current
                val active  = i == current
                val dotSize by animateDpAsState(if (active) 13.dp else 9.dp, label = "dot$i")
                val dotColor by animateColorAsState(
                    targetValue = if (done) colorDarkOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    label = "dc$i"
                )
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .clip(CircleShape)
                        .background(dotColor)
                        .then(if (active) Modifier.border(2.dp, colorDarkOrange.copy(alpha = 0.35f), CircleShape) else Modifier)
                )
                if (i < STEPS.lastIndex) {
                    val lineColor by animateColorAsState(
                        if (i < current) colorDarkOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                        label = "lc$i"
                    )
                    Box(
                        modifier = Modifier.weight(1f).height(2.dp)
                            .clip(RoundedCornerShape(1.dp)).background(lineColor)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            STEPS.forEachIndexed { i, label ->
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    textAlign = when (i) {
                        0              -> androidx.compose.ui.text.style.TextAlign.Start
                        STEPS.lastIndex -> androidx.compose.ui.text.style.TextAlign.End
                        else           -> androidx.compose.ui.text.style.TextAlign.Center
                    },
                    fontFamily  = SoraFontFamily,
                    fontSize    = 10.sp,
                    fontWeight  = if (i == current) FontWeight.W700 else FontWeight.W400,
                    color       = if (i <= current) colorDarkOrange else colorLightGrey.copy(alpha = 0.6f)
                )
            }
        }
        if (status == "PENDING") {
            Text(
                "⏳  Ожидаем подтверждения продавца...",
                fontFamily = SoraFontFamily, fontSize = 11.sp,
                color = Color(0xFFF57C00)
            )
        }
    }
}

// ─── Single item row ──────────────────────────────────────────────────────────

@Composable
private fun OrderLineItem(item: OrderItemResponse, currency: NumberFormat) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "${item.coffeeName} · ${item.selectedSize}",
            modifier = Modifier.weight(1f),
            fontFamily = SoraFontFamily, fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("× ${item.quantity}", fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorLightGrey)
            Text(currency.format(item.totalPrice), fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
