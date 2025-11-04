package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifeai_mobile.components.HistoricoImcComponent
import com.example.lifeai_mobile.viewmodel.HistoricoImcViewModel
import com.example.lifeai_mobile.viewmodel.ImcCalculatorViewModel
import com.example.lifeai_mobile.viewmodel.UiEvent
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcCalculatorScreen(
    navController: NavController,
    viewModel: ImcCalculatorViewModel,
    historicoViewModel: HistoricoImcViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val formattedDate by remember {
        derivedStateOf {
            val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(selectedMillis))
        }
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    // ### 2. ATUALIZA A TABELA QUANDO OCORRE SUCESSO ###
                    if (event.message.contains("sucesso", ignoreCase = true)) {
                        historicoViewModel.buscarHistorico()
                    }
                }
                is UiEvent.NavigateBack -> {
                    // Se você registrou com sucesso, atualize a tabela
                    historicoViewModel.buscarHistorico()
                    // Você pode querer que ele volte ou não
                    // navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let {
            viewModel.onDataChange(Date(it))
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Calculadora de IMC",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1A26)
                )
            )
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp), // Padding horizontal para os itens
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp)) // Espaço do topo
                Text(
                    "Preencha os dados abaixo para registrar seu progresso.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
            }

            item {
                OutlinedTextField(
                    value = viewModel.peso,
                    onValueChange = { viewModel.onPesoChange(it) },
                    label = { Text("Seu Peso (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color(0xFF4A90E2),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                OutlinedTextField(
                    value = viewModel.altura,
                    onValueChange = { viewModel.onAlturaChange(it) },
                    label = { Text("Sua Altura (m)") },
                    placeholder = { Text("Ex: 1.75") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                        cursorColor = Color(0xFF4A90E2),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Data da aferição",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = formattedDate,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendário",
                                tint = Color(0xFF4A90E2)
                            )
                        },
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = Color.White.copy(alpha = 0.4f),
                            disabledLeadingIconColor = Color(0xFF4A90E2),
                            disabledLabelColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
            item {
                val buttonBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E88E5),
                        Color(0xFF1565C0)
                    )
                )

                Button(
                    onClick = { viewModel.calculateAndRegister() },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(buttonBrush, shape = RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(26.dp),
                                color = Color.White
                            )
                        } else {
                            Text(
                                "Calcular e Registrar",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // ### 4. ADICIONA A TABELA DE HISTÓRICO ABAIXO ###
            item {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "Histórico Recente",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            item {
                HistoricoImcComponent(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp), // Dê uma altura fixa para a tabela
                    viewModel = historicoViewModel
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp)) // Espaço no final
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK", color = Color(0xFF4A90E2))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar", color = Color.White.copy(alpha = 0.7f))
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1B2A3D),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.White.copy(alpha = 0.7f),
                yearContentColor = Color.White,
                selectedYearContentColor = Color(0xFF4A90E2)
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
