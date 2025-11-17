package com.example.lifeai_mobile.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.components.HistoricoComposicaoComponent
import com.example.lifeai_mobile.viewmodel.AnaliseItem
import com.example.lifeai_mobile.viewmodel.AnaliseStatus
import com.example.lifeai_mobile.viewmodel.ComposicaoCorporalState
import com.example.lifeai_mobile.viewmodel.ComposicaoCorporalViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposicaoCorporalScreen(
    navController: NavController,
    viewModel: ComposicaoCorporalViewModel
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetStateManual = rememberModalBottomSheetState()
    val sheetStateEstimador = rememberModalBottomSheetState()

    // Controle dos sheets
    var showSheetManual by remember { mutableStateOf(false) }
    var showSheetEstimador by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Composição Corporal",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { showInfo = true }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Sobre Composição Corporal",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1A26))
            )
        },
        floatingActionButton = {
            if (state is ComposicaoCorporalState.Success) {
                FloatingActionButton(
                    onClick = { showSheetManual = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Registro")
                }
            }
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        when (val currentState = state) {
            is ComposicaoCorporalState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ComposicaoCorporalState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is ComposicaoCorporalState.Empty -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    onInserirManualClick = { showSheetManual = true },
                    onEstimarClick = { showSheetEstimador = true }
                )
            }
            is ComposicaoCorporalState.Success -> {
                SuccessState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    state = currentState
                )
            }
        }
    }

    // Sheet Manual
    if (showSheetManual) {
        ModalBottomSheet(
            onDismissRequest = { showSheetManual = false },
            sheetState = sheetStateManual,
            containerColor = Color(0xFF1B2A3D)
        ) {
            ManualEntrySheet(
                viewModel = viewModel,
                onSave = {
                    viewModel.salvarRegistroManual()
                    scope.launch {
                        sheetStateManual.hide()
                    }.invokeOnCompletion {
                        if (!sheetStateManual.isVisible) showSheetManual = false
                    }
                }
            )
        }
    }

    // Sheet Estimador
    if (showSheetEstimador) {
        ModalBottomSheet(
            onDismissRequest = { showSheetEstimador = false },
            sheetState = sheetStateEstimador,
            containerColor = Color(0xFF1B2A3D)
        ) {
            EstimativaSheetContent(
                viewModel = viewModel,
                onSave = {
                    viewModel.salvarRegistroEstimado()
                    scope.launch {
                        sheetStateEstimador.hide()
                    }.invokeOnCompletion {
                        if (!sheetStateEstimador.isVisible) showSheetEstimador = false
                    }
                }
            )
        }
    }

    // BottomSheet de Informações
    if (showInfo) {
        ModalBottomSheet(
            onDismissRequest = { showInfo = false },
            containerColor = Color(0xFF1B2A3D)
        ) {
            InfoComposicaoCorporalSheet(onClose = { showInfo = false })
        }
    }
}

// Estado vazio
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    onInserirManualClick: () -> Unit,
    onEstimarClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Analytics,
            contentDescription = "Composição Corporal",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Primeiro Registro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Adicione seu primeiro registro para começar a acompanhar sua evolução.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onInserirManualClick,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Registrar dados da balança") }
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onEstimarClick,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) { Text("Estimar com fita métrica") }
    }
}

// Estado de sucesso
@Composable
private fun SuccessState(
    modifier: Modifier = Modifier,
    state: ComposicaoCorporalState.Success
) {
    var showHistory by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.analise?.let { analise ->
            item {
                Text(
                    "Seus Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            item { AnaliseInsightCard("Gordura Corporal", analise.analiseGordura) }
            item { AnaliseInsightCard("Massa Muscular", analise.analiseMusculo) }
            item { AnaliseInsightCard("Hidratação", analise.analiseAgua) }
        }

        item {
            OutlinedButton(
                onClick = { showHistory = !showHistory },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(if (showHistory) "Esconder Histórico" else "Mostrar Histórico")
            }
        }

        item {
            AnimatedVisibility(visible = showHistory) {
                Column {
                    Text(
                        "Histórico",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                    )
                    HistoricoComposicaoComponent(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        registros = state.historico
                    )
                }
            }
        }
    }
}

// Card de insight
@Composable
private fun AnaliseInsightCard(titulo: String, item: AnaliseItem) {
    if (item.valor.isBlank()) return

    val statusColor = when (item.status) {
        AnaliseStatus.OTIMO -> Color(0xFF00C853)
        AnaliseStatus.BOM -> Color(0xFF4A90E2)
        AnaliseStatus.BAIXO -> Color(0xFFFDD835)
        AnaliseStatus.ALERTA -> Color(0xFFFF5252)
    }

    val statusIcon = when (item.status) {
        AnaliseStatus.OTIMO, AnaliseStatus.BOM -> Icons.Default.CheckCircle
        AnaliseStatus.BAIXO -> Icons.Default.Warning
        AnaliseStatus.ALERTA -> Icons.Default.Error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(statusIcon, contentDescription = item.status.name, tint = statusColor, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Text(item.mensagem, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(16.dp))
            Text(item.valor, color = statusColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

// Sheet Manual
@Composable
private fun ManualEntrySheet(viewModel: ComposicaoCorporalViewModel, onSave: () -> Unit) {
    val gordura by viewModel.gorduraPercentual.collectAsState()
    val musculo by viewModel.musculoPercentual.collectAsState()
    val agua by viewModel.aguaPercentual.collectAsState()
    val visceral by viewModel.gorduraVisceral.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Registro Manual", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        FormTextField(gordura, viewModel::onGorduraChange, "Gordura Corporal (%)")
        FormTextField(musculo, viewModel::onMusculoChange, "Massa Muscular (%)")
        FormTextField(agua, viewModel::onAguaChange, "Água Corporal (%)")
        FormTextField(visceral, viewModel::onVisceralChange, "Gordura Visceral (Nível)")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Salvar Registro")
        }
    }
}

// Sheet Estimativa
@Composable
private fun EstimativaSheetContent(viewModel: ComposicaoCorporalViewModel, onSave: () -> Unit) {
    val altura by viewModel.alturaEstimador.collectAsState()
    val pescoco by viewModel.pescocoEstimador.collectAsState()
    val cintura by viewModel.cinturaEstimador.collectAsState()
    val quadril by viewModel.quadrilEstimador.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Estimativa com Fita", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Icon(
            Icons.Default.Straighten,
            contentDescription = "Fita Métrica",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
        Text(
            "Use uma fita métrica e insira as medidas em centímetros (cm).",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        FormTextField(altura, viewModel::onAlturaEstimadorChange, "Altura (cm)")
        FormTextField(pescoco, viewModel::onPescocoEstimadorChange, "Pescoço (cm)")
        FormTextField(cintura, viewModel::onCinturaEstimadorChange, "Cintura (cm)")
        FormTextField(quadril, viewModel::onQuadrilEstimadorChange, "Quadril (cm) - Apenas se for mulher")
        Spacer(Modifier.height(16.dp))
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().height(50.dp)) {
            Text("Calcular e Salvar Estimativa")
        }
    }
}

// Campo de texto
@Composable
private fun FormTextField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true
    )
}

// BottomSheet de Informações
@Composable
fun InfoComposicaoCorporalSheet(onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "O que é Composição Corporal?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Composição corporal é a análise dos elementos que formam o corpo: gordura, massa muscular, água e gordura visceral. Esta avaliação fornece uma visão mais completa da saúde do que apenas o peso.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            "Como registrar:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            "Registro manual: use os dados fornecidos pela sua balança inteligente.\n\n" +
                    "Estimativa com fita métrica: informe altura, pescoço, cintura e quadril (mulheres). O app calcula automaticamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Entendi")
        }
    }
}