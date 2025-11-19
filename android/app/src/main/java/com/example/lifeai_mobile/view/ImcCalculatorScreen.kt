package com.example.lifeai_mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.components.HistoricoImcComponent
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.viewmodel.HistoricoImcViewModel
import com.example.lifeai_mobile.viewmodel.ImcCalculatorViewModel
import com.example.lifeai_mobile.viewmodel.UiEvent
import java.util.*
import com.example.lifeai_mobile.viewmodel.ImcHistoryState
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
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
    val historicoState by historicoViewModel.state.collectAsState()

    var showFormSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showHeightUnlockDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    val formattedDate by remember {
        derivedStateOf {
            val millis = viewModel.dataConsulta.time
            val localDate = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.of("UTC"))
                .toLocalDate()
            localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
    }

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
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showFormSheet = false
                    }
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFormSheet = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Registrar IMC")
            }
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->

        var showHistory by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (val currentState = historicoState) {
                is ImcHistoryState.Loading -> {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
                is ImcHistoryState.Error -> {
                    item {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp)
                        )
                    }
                }
                is ImcHistoryState.Success -> {
                    if (currentState.historico.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(top = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonitorHeart,
                                    contentDescription = "IMC",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Nenhum registro",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Clique no botão '+' para adicionar seu primeiro registro de IMC.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        val ultimoRegistro = currentState.historico.first()
                        item {
                            Text(
                                "Seu Último Registro",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        item {
                            ImcInsightCard(registro = ultimoRegistro)
                        }

                        // --- TABELA DE REFERÊNCIA ADICIONADA AQUI ---
                        item {
                            ImcReferenceTable()
                        }

                        item {
                            OutlinedButton(
                                onClick = { showHistory = !showHistory },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                            ) {
                                Text(if (showHistory) "Esconder Histórico" else "Mostrar Histórico")
                            }
                        }
                        item {
                            AnimatedVisibility(visible = showHistory) {
                                Column {
                                    Text(
                                        text = "Histórico Completo",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                                    )
                                    HistoricoImcComponent(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(300.dp),
                                        viewModel = historicoViewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showFormSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFormSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1B2A3D)
        ) {
            ImcEntrySheet(
                viewModel = viewModel,
                onShowDatePicker = { showDatePicker = true },
                onShowHeightUnlockDialog = { showHeightUnlockDialog = true },
                formattedDate = formattedDate
            )
        }
    }

    if (showDatePicker) {
        val todayMillisUtc = remember {
            LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }

        val selectableDates = remember {
            object : SelectableDates {
                override fun isSelectableDate(millis: Long): Boolean {
                    return millis <= todayMillisUtc
                }
            }
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.dataConsulta.time,
            selectableDates = selectableDates
        )

        LaunchedEffect(datePickerState.selectedDateMillis) {
            datePickerState.selectedDateMillis?.let { millis ->
                val localDate = Instant.ofEpochMilli(millis)
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDate()

                val date = Date.from(
                    localDate.atStartOfDay(ZoneId.of("UTC")).toInstant()
                )
                viewModel.onDataChange(date)
            }
        }

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
                selectedYearContentColor = Color(0xFF4A90E2),
                currentYearContentColor = Color.White,
                selectedYearContainerColor = Color(0xFF4A90E2),
                dayContentColor = Color.White,
                selectedDayContainerColor = Color(0xFF4A90E2),
                selectedDayContentColor = Color.White,
                todayDateBorderColor = Color(0xFF4A90E2),
                todayContentColor = Color(0xFF4A90E2)
            )
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHeightUnlockDialog) {
        HeightUnlockDialog(
            onDismiss = { showHeightUnlockDialog = false },
            onConfirm = {
                viewModel.onUnlockHeightField()
                showHeightUnlockDialog = false
            }
        )
    }
}

@Composable
private fun ImcEntrySheet(
    viewModel: ImcCalculatorViewModel,
    onShowDatePicker: () -> Unit,
    onShowHeightUnlockDialog: () -> Unit,
    formattedDate: String
) {
    val buttonPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (buttonPressed) 0.97f else 1f, label = "")

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                "Novo Registro de IMC",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )
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
                        unfocusedTextColor = Color.White,
                        disabledTextColor = Color.White,
                        disabledBorderColor = Color.White.copy(alpha = 0.4f),
                        disabledLeadingIconColor = Color(0xFF4A90E2),
                        disabledTrailingIconColor = Color(0xFF4A90E2),
                        disabledLabelColor = Color.White.copy(alpha = 0.7f)
                    ),
                    singleLine = true,
                    enabled = !viewModel.isHeightFieldLocked,
                    trailingIcon = {
                        if (viewModel.isHeightFieldLocked) {
                            IconButton(onClick = onShowHeightUnlockDialog) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Editar Altura"
                                )
                            }
                        }
                    }
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
                        .clickable { onShowDatePicker() },
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
    }
}

@Composable
private fun ImcInsightCard(registro: ImcRegistro) {
    val imc = registro.imcRes.toFloat()

    val (statusColor, statusIcon) = when {
        imc < 18.5 -> Color(0xFF4A90E2) to Icons.Default.Warning
        imc < 25 -> Color(0xFF00C853) to Icons.Default.CheckCircle
        imc < 30 -> Color(0xFFFDD835) to Icons.Default.Warning
        else -> Color(0xFFFF5252) to Icons.Default.Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = registro.classificacao,
                tint = statusColor,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = registro.classificacao,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Seu último IMC registrado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = String.format(Locale.US, "%.1f", imc),
                color = statusColor,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
private fun ImcReferenceTable() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Tabela de Referência (OMS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(12.dp))

            ReferenceRow(
                range = "Menor que 18.5",
                label = "Abaixo do Peso",
                color = Color(0xFF4A90E2)
            )
            ReferenceRow(
                range = "18.5 - 24.9",
                label = "Peso Normal",
                color = Color(0xFF00C853)
            )
            ReferenceRow(
                range = "25.0 - 29.9",
                label = "Sobrepeso",
                color = Color(0xFFFDD835)
            )
            ReferenceRow(
                range = "30.0 ou mais",
                label = "Obesidade",
                color = Color(0xFFFF5252)
            )
        }
    }
}

@Composable
private fun ReferenceRow(range: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = range,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun HeightUnlockDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mudar sua Altura?", color = Color.White) },
        text = { Text("A maioria dos adultos não muda de altura. Tem certeza que quer atualizar este valor?", color = Color.LightGray) },
        containerColor = Color(0xFF1B2A3D),
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
            ) { Text("Mudar") }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.7f))
            ) { Text("Cancelar", color = Color.White.copy(alpha = 0.7f)) }
        }
    )
}