package com.example.lifeai_mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import com.example.lifeai_mobile.viewmodel.ImcHistoryState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImcCalculatorScreen(
    navController: NavController,
    viewModel: ImcCalculatorViewModel,
    historicoViewModel: HistoricoImcViewModel
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())

    val formattedDate by remember {
        derivedStateOf {
            val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
            val localDate = Instant.ofEpochMilli(selectedMillis).atZone(ZoneId.of("UTC")).toLocalDate()
            localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }
    var lastImc by remember { mutableStateOf<Float?>(null) }
    var lastStatus by remember { mutableStateOf<String?>(null) }

    val historicoState by historicoViewModel.state.collectAsState()

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    if (event.message.contains("sucesso", ignoreCase = true)) {
                        historicoViewModel.buscarHistorico()
                    }
                }
                is UiEvent.NavigateBack -> {
                    historicoViewModel.buscarHistorico()
                }
            }
        }
    }

    LaunchedEffect(historicoState) {
        if (historicoState is ImcHistoryState.Success) {
            val historico = (historicoState as ImcHistoryState.Success).historico
            if (historico.isNotEmpty()) {
                val ultimaAltura = historico.first().altura
                if (viewModel.altura.isBlank() && ultimaAltura > 0) {
                    var alturaMetros = ultimaAltura
                    if (alturaMetros > 3) alturaMetros /= 100f
                    viewModel.onAlturaChange(String.format(Locale.US, "%.2f", alturaMetros))
                }
            }
        }
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
            val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
            viewModel.onDataChange(date)
        }
    }

    val buttonPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (buttonPressed) 0.97f else 1f, label = "")

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Calculadora de IMC", fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1A26))
            )
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Acompanhe sua evolução registrando seu IMC.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.peso,
                        onValueChange = { viewModel.onPesoChange(it) },
                        label = { Text("Peso (kg)") },
                        leadingIcon = {
                            Icon(Icons.Default.MonitorWeight, contentDescription = null, tint = Color(0xFF4A90E2))
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            focusedLabelColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color(0xFF4A90E2),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.altura,
                        onValueChange = { viewModel.onAlturaChange(it) },
                        label = { Text("Altura (m)") },
                        leadingIcon = {
                            Icon(Icons.Default.Height, contentDescription = null, tint = Color(0xFF4A90E2))
                        },
                        placeholder = { Text("Ex: 1.75") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4A90E2),
                            focusedLabelColor = Color(0xFF4A90E2),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                            cursorColor = Color(0xFF4A90E2),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }

            item {
                Column(Modifier.fillMaxWidth()) {
                    Text(
                        "Data da aferição",
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
                            Icon(Icons.Default.DateRange, contentDescription = "Calendário", tint = Color(0xFF4A90E2))
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

            item { Spacer(Modifier.height(32.dp)) }

            item {
                val buttonBrush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF00ACC1))
                )
                Button(
                    onClick = {
                        viewModel.calculateAndRegister()
                    },
                    enabled = !viewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .scale(scale),
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
                            CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Color.White)
                        } else {
                            Text("Calcular e Registrar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }

            item {
                AnimatedVisibility(visible = lastImc != null, enter = fadeIn(), exit = fadeOut()) {
                    lastImc?.let { imc ->
                        val cor = when {
                            imc < 18.5 -> Color(0xFF42A5F5)
                            imc < 25 -> Color(0xFF66BB6A)
                            imc < 30 -> Color(0xFFFFB300)
                            else -> Color(0xFFE53935)
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = cor.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Último IMC", color = cor, fontWeight = FontWeight.Bold)
                                Text(String.format("%.1f - %s", imc, lastStatus ?: ""), color = Color.White, fontSize = MaterialTheme.typography.titleLarge.fontSize)
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }

            item {
                Text(
                    "Histórico Recente",
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
                        .height(300.dp),
                    viewModel = historicoViewModel
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2)
                    )
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDatePicker = false },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
                ) {
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