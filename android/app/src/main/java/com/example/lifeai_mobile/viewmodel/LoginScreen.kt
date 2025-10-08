package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lifeai_mobile.R
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.view.AuthViewModel
import com.example.lifeai_mobile.view.AuthViewModelFactory

@Composable
fun LoginScreen(navController: NavController) {
    val repository = remember { AuthRepository(RetrofitInstance.api) }
    val factory = remember { AuthViewModelFactory(repository) }
    val authViewModel: AuthViewModel = viewModel(factory = factory)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loginResponse by authViewModel.loginResponse.collectAsState()
    val errorState by authViewModel.errorMessage.collectAsState()
    var displayMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(loginResponse) {
        loginResponse?.let {
            navController.navigate("onboarding") {
                launchSingleTop = true
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    LaunchedEffect(errorState) {
        errorState?.let {
            displayMessage = it
            isError = true
        }
    }

    val cardBackgroundColor = Color(0xFF161B22)
    val accentColor = Color(0xFF58C4D3)
    val borderColor = Color(0xFF5A5A5A)
    val backButtonBackgroundColor = Color(0xFF2D333B)
    val textColorPrimary = Color.White
    val textColorSecondary = Color(0xFF8B949E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .systemBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(backButtonBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { navController.popBackStack("welcome", inclusive = false) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = accentColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Image(
                painter = painterResource(id = R.drawable.lifeai_logo),
                contentDescription = "LifeAI Logo",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bem-vindo de volta!",
                style = TextStyle(
                    color = textColorPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .align(Alignment.BottomCenter),
            color = cardBackgroundColor,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "Login",
                    style = TextStyle(
                        color = textColorPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        if (displayMessage != null) authViewModel.clearErrorMessage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("admin@email.com") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = accentColor) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        cursorColor = accentColor,
                        focusedTextColor = textColorPrimary,
                        unfocusedTextColor = textColorPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedPlaceholderColor = textColorSecondary,
                        unfocusedPlaceholderColor = textColorSecondary,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        if (displayMessage != null) authViewModel.clearErrorMessage()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Senha", tint = accentColor) },
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = borderColor,
                        cursorColor = accentColor,
                        focusedTextColor = textColorPrimary,
                        unfocusedTextColor = textColorPrimary,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedPlaceholderColor = textColorSecondary,
                        unfocusedPlaceholderColor = textColorSecondary,
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))

                OutlinedButton(
                    onClick = {
                        isError = false
                        displayMessage = null
                        authViewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, accentColor),
                ) {
                    Text(
                        "Entrar",
                        fontSize = 18.sp,
                        color = textColorPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (displayMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = displayMessage ?: "",
                        color = if (isError) MaterialTheme.colorScheme.error else Color(0xFF238636),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(
                    onClick = {
                        navController.navigate("createAccount") {
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.padding(bottom = 24.dp, top = 16.dp)
                ) {
                    Text(
                        "Não possui uma conta? Registre-se",
                        color = textColorSecondary,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}