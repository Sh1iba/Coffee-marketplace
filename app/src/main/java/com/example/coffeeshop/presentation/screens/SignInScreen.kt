package com.example.coffeeshop.presentation.screens


import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.material3.MaterialTheme
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coffeeshop.navigation.NavigationRoutes
import com.example.coffeeshop.presentation.theme.CoffeeShopTheme
import com.example.coffeeshop.presentation.theme.colorDarkOrange
import com.example.coffeeshop.presentation.theme.colorLightGrey
import com.example.coffeeshop.presentation.theme.colorLightRecGrey
import com.example.coffeeshop.presentation.viewmodel.SignInViewModel

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SignInScreen(navController: NavController) {

    CoffeeShopTheme() {
        val context = LocalContext.current
        val viewModel: SignInViewModel = hiltViewModel()

        val state by viewModel.uiState.collectAsState()

        LaunchedEffect(state.errorMessage) {
            state.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }


        LaunchedEffect(state.isLoginSuccess) {
            if (state.isLoginSuccess) {
                val destination = if (state.loggedInRole == "ADMIN") {
                    NavigationRoutes.ADMIN_DASHBOARD
                } else {
                    NavigationRoutes.HOME
                }
                navController.navigate(destination) {
                    popUpTo(NavigationRoutes.SIGN_IN) { inclusive = true }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 196.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Войти в Аккаунт",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W600,
                    fontSize = 20.sp,
                    lineHeight = 24.sp,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .wrapContentSize()
                )

                Text(
                    text = "С возвращением, мы скучали по тебе.",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp,
                    lineHeight = 19.2.sp,
                    color = colorLightGrey,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.1f.dp)
                )
            }

            Text(
                text = "Почта",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .offset(x = 24.dp, y = 305.dp)
            )

            Box(
                modifier = Modifier
                    .padding(start = 22.dp, top = 330.dp, end = 22.dp)
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
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .width(62.dp)
                    .height(24.dp)
                    .offset(x = 24.dp, y = 373.dp)
            )

            Box(
                modifier = Modifier
                    .padding(start = 22.dp, top = 399.dp, end = 22.dp)
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

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "забыли Пароль?",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = colorDarkOrange,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 22.dp, top = 441.dp)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 39.dp, top = 473.dp, end = 39.dp),
                thickness = 1.dp,
                color = Color(0xFFE3E3E3)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 489.dp, end = 24.dp)
            ) {
                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorDarkOrange
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        Text("Загрузка...", color = Color.White)
                    } else {
                        Text(
                            text = "Войти",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.W600,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color.White
                        )
                    }
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
                                color = MaterialTheme.colorScheme.outline
                            )
                        ) {
                            append("У вас нет учетной записи? ")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = colorDarkOrange,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Создать")
                        }
                    },
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.W400,
                    fontSize = 16.sp,
                    lineHeight = 19.2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(19.dp)
                        .clickable {
                            navController.navigate(NavigationRoutes.REGISTRATION) {
                                popUpTo(NavigationRoutes.SIGN_IN) { inclusive = true }
                            }
                        }
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true, name = "pre")
@Composable
fun SignInScreenPreview() {
    val navController = rememberNavController()
    SignInScreen(navController = navController)
}