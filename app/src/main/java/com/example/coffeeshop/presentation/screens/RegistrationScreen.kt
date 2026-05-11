@file:Suppress("ComposePreviewMustBeTopLevelFunction")

package com.example.coffeeshop.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.coffeeshop.presentation.theme.SoraFontFamily
import com.example.coffeeshop.presentation.theme.colorFoundationGrey
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.coffeeshop.R
import com.example.coffeeshop.navigation.NavigationRoutes
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.presentation.theme.colorBackgroudWhite
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorLightGrey
import com.example.coffeeshop.presentation.theme.colorLightRecGrey
import com.example.coffeeshop.presentation.viewmodel.RegistrationViewModel

@Composable
fun RegistrationScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: RegistrationViewModel = hiltViewModel()

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBackgroudWhite)
    ) {

        Text(
            text = "Создать Аккаунт",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W600,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            color = colorFoundationGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(162.dp)
                .height(24.dp)
                .offset(x = 107.dp, y = 163.dp)
        )

        Text(
            text = "Добавьте информацию о себе.",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 19.2.sp,
            color = colorLightGrey,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .width(300.dp)
                .height(19.dp)
                .offset(x = 38.dp, y = 203.dp)
        )

        Text(
            text = "Имя",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = colorFoundationGrey,
            modifier = Modifier
                .width(49.dp)
                .height(24.dp)
                .offset(x = 24.dp, y = 267.dp)
        )

        Box(
            modifier = Modifier
                .padding(start = 22.dp, top = 291.dp, end = 22.dp)
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorLightRecGrey),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                ),
                singleLine = true
            )
        }

        Text(
            text = "Почта",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = colorFoundationGrey,
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .offset(x = 24.dp, y = 336.dp)
        )

        Box(
            modifier = Modifier
                .padding(start = 22.dp, top = 360.dp, end = 22.dp)
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorLightRecGrey),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                ),
                singleLine = true
            )
        }

        Text(
            text = "Пароль",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            color = colorFoundationGrey,
            modifier = Modifier
                .width(62.dp)
                .height(24.dp)
                .offset(x = 24.dp, y = 403.dp)
        )

        Box(
            modifier = Modifier
                .padding(start = 22.dp, top = 427.dp, end = 22.dp)
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorLightRecGrey),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = state.password,
                onValueChange = viewModel::onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 16.sp
                ),
                visualTransformation = if (state.isPasswordVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                singleLine = true
            )

            IconButton(
                onClick = viewModel::onPasswordVisibilityChange,
                modifier = Modifier
                    .size(48.dp)
                    .align(alignment = Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(
                        id = if (state.isPasswordVisible)
                            R.drawable.eye_on_foreground
                        else
                            R.drawable.eye_off_foreground
                    ),
                    contentDescription = if (state.isPasswordVisible)
                        "Скрыть пароль"
                    else
                        "Показать пароль",
                    tint = colorFoundationGrey
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 39.dp, top = 475.dp, end = 39.dp),
            thickness = 1.dp,
            color = Color(0xFFE3E3E3)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 491.dp, end = 24.dp)
        ) {
            Button(
                onClick = { viewModel.register() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorDarkOrange
                ),
                enabled = !state.isLoading
            ) {
                Text(
                    text = "Зарегистрироваться",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier,
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 710.dp),
                thickness = 4.dp,
                color = Color(0xFFF9F2ED)
            )

            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = colorFoundationGrey
                        )
                    ) {
                        append("У вас уже есть учетная запись? ")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = colorDarkOrange,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append("Войти")
                    }
                },
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 19.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.clickable {
                    navController.navigate(NavigationRoutes.SIGN_IN) {
                        popUpTo(NavigationRoutes.REGISTRATION) { inclusive = true }
                    }
                }
            )
        }

        // Success Dialog
        if (state.showSuccessDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(start = 24.dp, end = 24.dp, top = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.success),
                        contentDescription = "Success",
                        modifier = Modifier.size(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Поздравляю!",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W600,
                        fontSize = 20.sp,
                        color = Color.Black
                    )

                    Text(
                        text = "Регистрация прошла успешно.",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.W400,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            navController.navigate(NavigationRoutes.SIGN_IN) {
                                popUpTo(NavigationRoutes.REGISTRATION) { inclusive = true }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorDarkOrange
                        )
                    ) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "pre")
@Composable
fun RegistrationScreenPreview() {
    RegistrationScreen(navController = rememberNavController())
}