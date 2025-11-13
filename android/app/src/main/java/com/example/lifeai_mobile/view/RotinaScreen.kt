package com.example.lifeai_mobile.view

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.viewmodel.RotinaUIState
import com.example.lifeai_mobile.viewmodel.RotinaViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotinaScreen(
    navController: NavController,
    viewModel: RotinaViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    Log.d("RotinaScreen", "Recompondo a tela. Estado: $state")
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Agenda de Compromissos", fontWeight = FontWeight.Bold, color = Color.White) },
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
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF00C9A7),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Compromisso")
            }
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->

        // Mostra o Pop-up de Adicionar
        if (showAddDialog) {
            AddCompromissoDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { titulo, data, horaInicio, horaFim ->
                    viewModel.adicionarCompromisso(titulo, data, horaInicio, horaFim)
                    showAddDialog = false
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val currentState = state) {
                is RotinaUIState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A90E2))
                    }
                }
                is RotinaUIState.Error -> {
                    ErrorState(
                        message = currentState.message,
                        onRetryClick = { viewModel.carregarCompromissos() }
                    )
                }
                is RotinaUIState.Success -> {
                    CompromissoList(
                        compromissos = currentState.compromissos,
                        onDelete = { compromisso ->
                            viewModel.deletarCompromisso(compromisso)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color.Red.copy(alpha = 0.8f),
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Erro ao Carregar",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Text("Tentar Novamente", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompromissoList(
    compromissos: List<Compromisso>,
    onDelete: (Compromisso) -> Unit
) {
    if (compromissos.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.EventNote,
                    contentDescription = "Sem compromissos",
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFF4A90E2)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Agenda Limpa",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Text(
                    "Adicione seus compromissos no botão '+'",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(compromissos, key = { it.id!! }) { compromisso ->
            CompromissoItemCard(
                compromisso = compromisso,
                onDelete = { onDelete(compromisso) }
            )
        }
    }
}

@Composable
private fun CompromissoItemCard(
    compromisso: Compromisso,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = compromisso.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Data: ${compromisso.data}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "${compromisso.hora_inicio} - ${compromisso.hora_fim}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4A90E2)
                )
            }
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Deletar Compromisso",
                    tint = Color.Gray
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCompromissoDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, data: String, horaInicio: String, horaFim: String) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    var titulo by remember { mutableStateOf("") }
    var data by remember { mutableStateOf(LocalDate.now()) }
    var horaInicio by remember { mutableStateOf(LocalTime.now().plusHours(1)) }
    var horaFim by remember { mutableStateOf(LocalTime.now().plusHours(2)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    // --- CORREÇÃO AQUI: LÓGICA PARA BLOQUEAR DATAS PASSADAS ---
    // 1. Pega o início do dia de "hoje" em UTC (em milissegundos)
    val todayMillisUtc = remember {
        LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
    // 2. Cria a regra: só datas >= "hoje" são selecionáveis.
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayMillisUtc
            }
        }
    }

    // --- FIM DA CORREÇÃO ---

    val datePickerState = rememberDatePickerState(
        // Começa selecionando "hoje"
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
        selectableDates = selectableDates // <-- 3. APLICA A REGRA
    )
    val startTimePickerState = rememberTimePickerState(
        initialHour = horaInicio.hour,
        initialMinute = horaInicio.minute,
        is24Hour = true
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = horaFim.hour,
        initialMinute = horaFim.minute,
        is24Hour = true
    )

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { millis ->
            data = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A90E2))
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) { Text("Cancelar") }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF1B2A3D),
                titleContentColor = Color.White,
                headlineContentColor = Color.White,
                weekdayContentColor = Color.White.copy(alpha = 0.7f),
                yearContentColor = Color.White,
                currentYearContentColor = Color.White,
                selectedYearContainerColor = Color(0xFF4A90E2),
                selectedYearContentColor = Color.White,
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

    if (showStartTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        horaInicio = LocalTime.of(startTimePickerState.hour, startTimePickerState.minute)
                        showStartTimePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A90E2))
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartTimePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) { Text("Cancelar") }
            }
        ) {
            TimePicker(
                state = startTimePickerState,
                colors = TimePickerDefaults.colors(
                    containerColor = Color(0xFF1B2A3D),
                    clockDialColor = Color(0xFF2C3E50),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    selectorColor = Color(0xFF4A90E2),
                    periodSelectorBorderColor = Color(0xFF4A90E2),
                    periodSelectorSelectedContainerColor = Color(0xFF4A90E2),
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    timeSelectorSelectedContainerColor = Color(0xFF4A90E2),
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContainerColor = Color(0xFF2C3E50),
                    timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        horaFim = LocalTime.of(endTimePickerState.hour, endTimePickerState.minute)
                        showEndTimePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A90E2))
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndTimePicker = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.White.copy(alpha = 0.7f))
                ) { Text("Cancelar") }
            }
        ) {
            TimePicker(
                state = endTimePickerState,
                colors = TimePickerDefaults.colors(
                    containerColor = Color(0xFF1B2A3D),
                    clockDialColor = Color(0xFF2C3E50),
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    selectorColor = Color(0xFF4A90E2),
                    periodSelectorBorderColor = Color(0xFF4A90E2),
                    periodSelectorSelectedContainerColor = Color(0xFF4A90E2),
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContainerColor = Color.Transparent,
                    periodSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f),
                    timeSelectorSelectedContainerColor = Color(0xFF4A90E2),
                    timeSelectorSelectedContentColor = Color.White,
                    timeSelectorUnselectedContainerColor = Color(0xFF2C3E50),
                    timeSelectorUnselectedContentColor = Color.White.copy(alpha = 0.7f)
                )
            )
        }
    }


    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Compromisso", fontWeight = FontWeight.Bold, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título do Compromisso") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF4A90E2),
                        cursorColor = Color(0xFF4A90E2),
                        focusedLabelColor = Color(0xFF4A90E2),
                        unfocusedLabelColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Text("Data", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                ) {
                    Text(data.format(dateFormatter))
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Início", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            onClick = { showStartTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text(horaInicio.format(timeFormatter))
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Fim", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                        OutlinedButton(
                            onClick = { showEndTimePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text(horaFim.format(timeFormatter))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        titulo,
                        data.toString(), // "YYYY-MM-DD"
                        horaInicio.format(timeFormatter), // "HH:MM"
                        horaFim.format(timeFormatter) // "HH:MM"
                    )
                },
                enabled = titulo.isNotBlank() && horaFim.isAfter(horaInicio),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C9A7))
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color(0xFF4A90E2))
            }
        },
        containerColor = Color(0xFF1B2A3D)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String = "Selecione a Hora",
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                content()
            }
        },
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        containerColor = Color(0xFF1B2A3D)
    )
}