package com.example.coffeeshop.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.twotone.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.*
import com.example.coffeeshop.presentation.viewmodel.BecomeSellerViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

private val SELLER_CATEGORIES = listOf(
    "Кофейня", "Пекарня", "Фастфуд", "Пиццерия", "Суши и роллы",
    "Восточная кухня", "Здоровое питание", "Десерты", "Завтраки", "Другое"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BecomeSellerScreen(navController: NavController) {
    val viewModel: BecomeSellerViewModel = hiltViewModel()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.success.collectAsState()

    var step by remember { mutableIntStateOf(0) }

    // Шаг 1 — Основная информация
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Шаг 2 — Контакты
    var phone by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }

    // Баннер
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bannerUrl by remember { mutableStateOf("") }
    var bannerPreviewUri by remember { mutableStateOf<Uri?>(null) }
    val isBannerUploading by viewModel.isBannerUploading.collectAsState()

    val bannerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            bannerPreviewUri = it
            scope.launch {
                try {
                    val mimeType = context.contentResolver.getType(it) ?: "image/jpeg"
                    val ext = when (mimeType) { "image/png" -> "png"; "image/gif" -> "gif"; "image/webp" -> "webp"; else -> "jpg" }
                    val bytes = context.contentResolver.openInputStream(it)?.readBytes() ?: return@launch
                    val body = bytes.toRequestBody(mimeType.toMediaType())
                    val part = MultipartBody.Part.createFormData("file", "upload.$ext", body)
                    viewModel.uploadBanner(part) { url -> if (url != null) bannerUrl = url }
                } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(success) {
        if (success) {
            navController.navigate(NavigationRoutes.SELLER_DASHBOARD) {
                popUpTo(NavigationRoutes.SETTINGS) { inclusive = false }
            }
        }
    }

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
                    IconButton(onClick = {
                        if (step > 0) step-- else navController.popBackStack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        "Открыть магазин",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Индикатор шагов
                StepIndicator(currentStep = step, totalSteps = 3)

                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
                }
            },
            label = "step_transition"
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (currentStep) {

                    // ── Шаг 1: Основное ──────────────────────────────────────
                    0 -> {
                        StepHeader(
                            icon = Icons.TwoTone.ShoppingCart,
                            title = "Основная информация",
                            subtitle = "Как называется ваш магазин и что вы предлагаете?"
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Название магазина *", fontFamily = SoraFontFamily) },
                            placeholder = { Text("например, Urban Brew", fontFamily = SoraFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.TwoTone.Place, null, tint = colorDarkOrange) },
                            colors = fieldColors()
                        )

                        ExposedDropdownMenuBox(
                            expanded = categoryExpanded,
                            onExpandedChange = { categoryExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = category,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Категория *", fontFamily = SoraFontFamily) },
                                placeholder = { Text("Выберите категорию", fontFamily = SoraFontFamily) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryExpanded) },
                                leadingIcon = { Icon(Icons.TwoTone.List, null, tint = colorDarkOrange) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryExpanded,
                                onDismissRequest = { categoryExpanded = false }
                            ) {
                                SELLER_CATEGORIES.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, fontFamily = SoraFontFamily) },
                                        onClick = { category = cat; categoryExpanded = false }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 500) description = it },
                            label = { Text("Описание *", fontFamily = SoraFontFamily) },
                            placeholder = { Text("Расскажите о вашем магазине, кухне, особенностях...", fontFamily = SoraFontFamily) },
                            modifier = Modifier.fillMaxWidth().height(130.dp),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 5,
                            supportingText = { Text("${description.length}/500", fontFamily = SoraFontFamily, fontSize = 11.sp) },
                            colors = fieldColors()
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Баннер магазина *",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.W600,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Главное фото, которое видят покупатели в каталоге",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { bannerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bannerPreviewUri != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context).data(bannerPreviewUri).build(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    if (isBannerUploading) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                                        }
                                    }
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.TwoTone.AddCircle, null, modifier = Modifier.size(32.dp), tint = colorDarkOrange)
                                        Text("Нажмите чтобы добавить баннер", fontFamily = SoraFontFamily, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            if (bannerUrl.isNotBlank()) {
                                Text("✓ Баннер загружен", fontFamily = SoraFontFamily, fontSize = 12.sp, color = colorDarkOrange)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        NextButton(
                            text = "Далее — Контакты",
                            enabled = name.isNotBlank() && category.isNotBlank() && description.length >= 20 && (bannerUrl.isNotBlank() || bannerPreviewUri != null),
                            onClick = { step = 1 }
                        )
                        if (description.isNotBlank() && description.length < 20) {
                            Text(
                                "Описание должно быть не менее 20 символов",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        if (bannerPreviewUri == null && bannerUrl.isBlank() && (name.isNotBlank() || description.isNotBlank())) {
                            Text(
                                "Добавьте баннер магазина",
                                fontFamily = SoraFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // ── Шаг 2: Контакты ──────────────────────────────────────
                    1 -> {
                        StepHeader(
                            icon = Icons.TwoTone.Phone,
                            title = "Контактные данные",
                            subtitle = "Как покупатели и наши менеджеры смогут с вами связаться?"
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Телефон *", fontFamily = SoraFontFamily) },
                            placeholder = { Text("+7 999 123-45-67", fontFamily = SoraFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Icon(Icons.TwoTone.Phone, null, tint = colorDarkOrange) },
                            colors = fieldColors()
                        )

                        OutlinedTextField(
                            value = website,
                            onValueChange = { website = it },
                            label = { Text("Сайт (необязательно)", fontFamily = SoraFontFamily) },
                            placeholder = { Text("https://yourshop.ru", fontFamily = SoraFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            leadingIcon = { Icon(Icons.TwoTone.Share, null, tint = colorDarkOrange) },
                            colors = fieldColors()
                        )

                        Spacer(Modifier.height(8.dp))
                        NextButton(
                            text = "Далее — Подтверждение",
                            enabled = phone.isNotBlank(),
                            onClick = { step = 2 }
                        )
                    }

                    // ── Шаг 3: Подтверждение ─────────────────────────────────
                    2 -> {
                        StepHeader(
                            icon = Icons.TwoTone.CheckCircle,
                            title = "Проверьте данные",
                            subtitle = "Убедитесь что всё заполнено верно перед отправкой на модерацию"
                        )

                        // Карточка-сводка
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                ReviewRow(label = "Название", value = name)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ReviewRow(label = "Категория", value = category)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ReviewRow(label = "Описание", value = description)
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                ReviewRow(label = "Телефон", value = phone)
                                if (website.isNotBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    ReviewRow(label = "Сайт", value = website)
                                }
                            }
                        }

                        // Инфо о модерации
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF8E1),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(Icons.TwoTone.Info, null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "После отправки магазин будет проверен",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.W600,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Модерация занимает до 24 часов. Вы получите доступ к панели продавца сразу, но магазин появится в каталоге только после одобрения.",
                                        fontFamily = SoraFontFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        error?.let { msg ->
                            Text(msg, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                        }

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = {
                                viewModel.register(
                                    name = name,
                                    description = description,
                                    category = category,
                                    phone = phone,
                                    website = website.ifBlank { null },
                                    logoUrl = bannerUrl.ifBlank { null }
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Icon(Icons.TwoTone.Check, null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Отправить на модерацию",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.W600,
                                    fontSize = 15.sp
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
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val stepLabels = listOf("Основное", "Контакты", "Подтверждение")
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isDone = index < currentStep

            // Кружок
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone   -> colorDarkOrange
                            isActive -> colorDarkOrange
                            else     -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(Icons.TwoTone.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                } else {
                    Text(
                        "${index + 1}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W700,
                        fontSize = 12.sp,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Лейбл + линия
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stepLabels[index],
                    fontFamily = SoraFontFamily,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.W600 else FontWeight.W400,
                    color = if (isActive || isDone) colorDarkOrange else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (index < totalSteps - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        color = if (isDone) colorDarkOrange else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        thickness = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun StepHeader(icon: ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colorDarkOrange.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = colorDarkOrange, modifier = Modifier.size(24.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontFamily = SoraFontFamily, fontWeight = FontWeight.W700, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)
            Text(subtitle, fontFamily = SoraFontFamily, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, fontFamily = SoraFontFamily, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = SoraFontFamily, fontWeight = FontWeight.W500, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun NextButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorDarkOrange)
    ) {
        Text(text, fontFamily = SoraFontFamily, fontWeight = FontWeight.W600, fontSize = 15.sp)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.TwoTone.ArrowForward, null, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = colorDarkOrange,
    focusedLabelColor = colorDarkOrange,
    cursorColor = colorDarkOrange
)
