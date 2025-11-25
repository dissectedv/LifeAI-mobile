package com.example.lifeai_mobile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.viewmodel.InputType
import com.example.lifeai_mobile.viewmodel.OnboardingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavController, onboardingViewModel: OnboardingViewModel) {

    val currentStep by onboardingViewModel.currentStep.collectAsState()
    val currentStepIndex by onboardingViewModel.currentStepIndex.collectAsState()
    val isButtonEnabled = onboardingViewModel.isNextButtonEnabled

    val uiState by onboardingViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        onboardingViewModel.navigateToHome.collect {
            navController.navigate("home") {
                popUpTo("welcome") { inclusive = true }
            }
        }
    }

    val backButtonBackgroundColor = Color(0xFF2D333B)
    val accentColor = Color(0xFF58C4D3)
    val textColorPrimary = Color.White
    val textColorSecondary = Color(0xFF8B949E)

    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentStep.progressText,
                        textAlign = TextAlign.Center,
                        color = textColorSecondary,
                        fontSize = 14.sp
                    )
                },
                navigationIcon = {
                    if (currentStepIndex > 0) {
                        Box(
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(backButtonBackgroundColor),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = { onboardingViewModel.onBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = accentColor
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
        // --- 1. 'bottomBar' REMOVIDO DAQUI ---
    ) { innerPadding ->

        // --- 2. CORREÇÃO: Usando um Box para controlar o layout ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Padding do TopBar
        ) {
            // --- 3. ITEM 1: A Coluna que sobe com o teclado ---
            Column(
                modifier = Modifier
                    .fillMaxSize() // Ocupa todo o espaço
                    .padding(horizontal = 28.dp)
                    .imePadding(), // <-- O imePadding SÓ se aplica a esta Coluna
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                Text(
                    text = currentStep.questionText,
                    color = textColorPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(60.dp))

                // [O seu 'when (currentStep.inputType)' ... ]
                when (currentStep.inputType) {
                    InputType.TEXT -> {
                        if (currentStep.progressText == "1/6") {
                            CustomTextField(
                                value = onboardingViewModel.name,
                                onValueChange = { onboardingViewModel.onNameChange(it) },
                                isError = onboardingViewModel.nameError != null,
                                errorMessage = onboardingViewModel.nameError
                            )
                        } else {
                            CustomTextField(
                                value = onboardingViewModel.objective,
                                onValueChange = { onboardingViewModel.onObjectiveChange(it) },
                                isError = onboardingViewModel.objectiveError != null,
                                errorMessage = onboardingViewModel.objectiveError
                            )
                        }
                    }
                    InputType.GENDER -> GenderSelector(
                        selectedValue = onboardingViewModel.gender,
                        onValueSelect = { onboardingViewModel.gender = it },
                        accentColor = accentColor
                    )
                    InputType.NUMBER -> {
                        when (currentStep.progressText) {
                            "3/6" -> CustomTextField(
                                value = onboardingViewModel.age,
                                onValueChange = { onboardingViewModel.onAgeChange(it) },
                                keyboardType = KeyboardType.Number,
                                isError = onboardingViewModel.ageError != null,
                                errorMessage = onboardingViewModel.ageError
                            )
                            "4/6" -> CustomTextField(
                                value = onboardingViewModel.height,
                                onValueChange = { onboardingViewModel.onHeightChange(it) },
                                keyboardType = KeyboardType.Number,
                                isError = onboardingViewModel.heightError != null,
                                errorMessage = onboardingViewModel.heightError
                            )
                            "5/6" -> CustomTextField(
                                value = onboardingViewModel.weight,
                                onValueChange = { onboardingViewModel.onWeightChange(it) },
                                keyboardType = KeyboardType.Number,
                                isError = onboardingViewModel.weightError != null,
                                errorMessage = onboardingViewModel.weightError
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    // [O seu 'when (uiState)' com o Botão 'Próximo'...]
                    when (uiState) {
                        is OnboardingViewModel.UiState.Loading -> {
                            CircularProgressIndicator(color = accentColor)
                        }
                        else -> {
                            OutlinedButton(
                                onClick = { onboardingViewModel.onNext() },
                                enabled = isButtonEnabled,
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(28.dp),
                                border = BorderStroke(1.5.dp, if (isButtonEnabled) accentColor else textColorSecondary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isButtonEnabled) textColorPrimary else textColorSecondary,
                                    disabledContentColor = textColorSecondary
                                )
                            ) {
                                Text(
                                    text = if (currentStep.progressText == "6/6") "Vamos começar!" else "Próximo",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                if (uiState is OnboardingViewModel.UiState.Error) {
                    val errorMessage = (uiState as OnboardingViewModel.UiState.Error).message
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                // O Spacer flexível empurra tudo para cima, mas
                // o "texto de aviso" não está mais aqui
                Spacer(modifier = Modifier.weight(1f))
            }

            // --- 4. ITEM 2: O Texto de Aviso Fixo ---
            // Ele fica na Box "pai", alinhado embaixo
            // Ele NÃO tem imePadding, então o teclado vai cobri-lo
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // <-- A MÁGICA
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Usamos essa informação para oferecer recomendações de saúde mais adequadas.",
                    color = textColorSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                )
            }
        }
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            isError = isError,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color(0xFF58C4D3),
                unfocusedIndicatorColor = Color(0xFF8B949E),
                cursorColor = Color(0xFF58C4D3),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorIndicatorColor = MaterialTheme.colorScheme.error
            ),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp),
            singleLine = true
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun GenderSelector(selectedValue: String, onValueSelect: (String) -> Unit, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        GenderToggleButton("Masculino", selectedValue == "Masculino", accentColor) { onValueSelect("Masculino") }
        GenderToggleButton("Feminino", selectedValue == "Feminino", accentColor) { onValueSelect("Feminino") }
        GenderToggleButton("Outro/Não-Binário", selectedValue == "Outro/Não-Binário", accentColor) { onValueSelect("Outro/Não-Binário") }
    }
}

@Composable
private fun GenderToggleButton(
    text: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val buttonColors = if (isSelected) {
        ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black)
    } else {
        ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Color.White)
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = if (!isSelected) BorderStroke(1.5.dp, accentColor) else null,
        colors = buttonColors
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}