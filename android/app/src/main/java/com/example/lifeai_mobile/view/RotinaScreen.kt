package com.example.lifeai_mobile.view

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
        items(compromissos, key = { it.id }) { compromisso ->
            // Simplesmente chamamos o Card e passamos a função de deletar
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
    onDelete: () -> Unit // Recebe a função
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
            // Coluna de Textos (Título, Data, Hora)
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

            // --- PLANO C: BOTÃO DE LIXEIRA ---
            Spacer(Modifier.width(12.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Deletar Compromisso",
                    tint = Color.Gray // Cor suave
                )
            }
        }
    }
}

@Composable
private fun AddCompromissoDialog(
    onDismiss: () -> Unit,
    onConfirm: (titulo: String, data: String, horaInicio: String, horaFim: String) -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var data by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFim by remember { mutableStateOf("") }
    val isFormValid = titulo.isNotBlank() && data.isNotBlank() && horaInicio.isNotBlank() && horaFim.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Compromisso", color = Color.White) },
        text = {
            Column {
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Data (YYYY-MM-DD)") },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    TextField(
                        value = horaInicio,
                        onValueChange = { horaInicio = it },
                        label = { Text("Início (HH:MM)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    TextField(
                        value = horaFim,
                        onValueChange = { horaFim = it },
                        label = { Text("Fim (HH:MM)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(titulo, data, horaInicio, horaFim) },
                enabled = isFormValid,
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