package com.example.lifeai_mobile.view

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Minha Rotina", fontWeight = FontWeight.Bold, color = Color.White) },
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
                    RotinaContent(
                        compromissos = currentState.compromissos,
                        onToggleConcluido = { viewModel.toggleConcluido(it) },
                        onDelete = { viewModel.deletarCompromisso(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RotinaContent(
    compromissos: List<Compromisso>,
    onToggleConcluido: (Compromisso) -> Unit,
    onDelete: (Compromisso) -> Unit
) {
    if (compromissos.isEmpty()) {
        EmptyState()
        return
    }

    val todayStr = LocalDate.now().toString()
    val tomorrowStr = LocalDate.now().plusDays(1).toString()

    val overdueTasks = compromissos.filter { it.data < todayStr && !it.concluido }
    val todayTasks = compromissos.filter { it.data == todayStr }
    val tomorrowTasks = compromissos.filter { it.data == tomorrowStr }
    val futureTasks = compromissos.filter { it.data > tomorrowStr }

    val totalToday = todayTasks.size
    val completedToday = todayTasks.count { it.concluido }
    val progress = if (totalToday > 0) completedToday.toFloat() / totalToday else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (totalToday > 0) {
            item {
                DailyProgressHeader(
                    completed = completedToday,
                    total = totalToday,
                    progress = progress
                )
            }
        }

        if (overdueTasks.isNotEmpty()) {
            item {
                Text(
                    text = "Pendentes",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFF5252),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            val sortedOverdue = overdueTasks.sortedBy { it.data }
            items(sortedOverdue, key = { it.id!! }) { task ->
                CompromissoItemCard(
                    compromisso = task,
                    isOverdue = true,
                    onCheckClick = { onToggleConcluido(task) },
                    onDelete = { onDelete(task) }
                )
            }
        }

        if (todayTasks.isNotEmpty()) {
            item { SectionHeader("Hoje") }
            val sortedToday = todayTasks.sortedWith(
                compareBy<Compromisso> { it.concluido }
                    .thenBy { it.hora_inicio }
            )
            items(sortedToday, key = { it.id!! }) { task ->
                CompromissoItemCard(
                    compromisso = task,
                    onCheckClick = { onToggleConcluido(task) },
                    onDelete = { onDelete(task) }
                )
            }
        }

        if (tomorrowTasks.isNotEmpty()) {
            item { SectionHeader("Amanhã") }
            val sortedTomorrow = tomorrowTasks.sortedBy { it.hora_inicio }
            items(sortedTomorrow, key = { it.id!! }) { task ->
                CompromissoItemCard(
                    compromisso = task,
                    onCheckClick = { onToggleConcluido(task) },
                    onDelete = { onDelete(task) }
                )
            }
        }

        if (futureTasks.isNotEmpty()) {
            item { SectionHeader("Futuro") }
            val sortedFuture = futureTasks.sortedWith(
                compareBy<Compromisso> { it.data }
                    .thenBy { it.hora_inicio }
            )
            items(sortedFuture, key = { it.id!! }) { task ->
                CompromissoItemCard(
                    compromisso = task,
                    onCheckClick = { onToggleConcluido(task) },
                    onDelete = { onDelete(task) }
                )
            }
        }

        item { Spacer(Modifier.height(60.dp)) }
    }
}

@Composable
private fun DailyProgressHeader(completed: Int, total: Int, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress"
    )
    val color by animateColorAsState(
        targetValue = if (progress >= 1f) Color(0xFF00C853) else Color(0xFF4A90E2),
        label = "color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Progresso Diário",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = Color.White.copy(alpha = 0.1f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$completed de $total tarefas concluídas",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = Color.White.copy(alpha = 0.9f),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun CompromissoItemCard(
    compromisso: Compromisso,
    isOverdue: Boolean = false,
    onCheckClick: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = compromisso.concluido

    val alpha by animateFloatAsState(targetValue = if (isCompleted) 0.5f else 1f, label = "alpha")

    val cardColor = if (isCompleted) Color(0xFF12212F) else Color(0xFF1B2A3D)
    val textColor = if (isCompleted) Color.White.copy(alpha = 0.5f) else Color.White
    val textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val checkColor = if (isCompleted) Color(0xFF00C9A7) else Color.White.copy(alpha = 0.3f)

    val horaInicio = try {
        LocalTime.parse(compromisso.hora_inicio.substring(0, 5))
    } catch (_: Exception) { LocalTime.NOON }

    val stripColor = when {
        isCompleted -> Color.Gray
        isOverdue -> Color(0xFFFF5252)
        horaInicio.hour < 12 -> Color(0xFFFFD54F)
        horaInicio.hour < 18 -> Color(0xFFFF8A65)
        else -> Color(0xFF9575CD)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 4.dp),
        border = if (isCompleted) BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)) else null
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(6.dp)
                    .background(stripColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isCompleted) checkColor else Color.Transparent)
                        .border(2.dp, checkColor, CircleShape)
                        .clickable { onCheckClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = compromisso.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textDecoration = textDecoration
                    )
                    Spacer(Modifier.height(4.dp))

                    val dateLabel = if (compromisso.data == LocalDate.now().toString()) "" else " • ${compromisso.data.split("-").reversed().joinToString("/")}"

                    Text(
                        text = "${compromisso.hora_inicio.take(5)} - ${compromisso.hora_fim.take(5)}$dateLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isCompleted) textColor else if (isOverdue) Color(0xFFFF5252) else stripColor
                    )

                    if (isOverdue && !isCompleted) {
                        Text(
                            text = "Pendente",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF5252),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.AutoMirrored.Filled.EventNote,
                contentDescription = "Sem compromissos",
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF4A90E2)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Agenda Limpa",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Adicione seus compromissos no botão '+'",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
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

    val todayMillisUtc = remember {
        LocalDate.now().atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
    }
    val selectableDates = remember {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayMillisUtc
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli(),
        selectableDates = selectableDates
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