package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var showSheetManual by remember { mutableStateOf(false) }
    var showSheetEstimador by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Composição Corporal",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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
                    scope.launch { sheetStateManual.hide() }.invokeOnCompletion {
                        if (!sheetStateManual.isVisible) {
                            showSheetManual = false
                        }
                    }
                }
            )
        }
    }

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
                    scope.launch { sheetStateEstimador.hide() }.invokeOnCompletion {
                        if (!sheetStateEstimador.isVisible) {
                            showSheetEstimador = false
                        }
                    }
                }
            )
        }
    }
}

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
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Primeiro Registro",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Adicione seu primeiro registro para começar a acompanhar sua evolução.",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onInserirManualClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Registrar dados da balança")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = onEstimarClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Estimar com fita métrica")
        }
    }
}

@Composable
private fun SuccessState(
    modifier: Modifier = Modifier,
    state: ComposicaoCorporalState.Success
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.analise?.let { analise ->
            item {
                Text(
                    text = "Seus Insights",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            item {
                AnaliseInsightCard(
                    titulo = "Gordura Corporal",
                    item = analise.analiseGordura
                )
            }
            item {
                AnaliseInsightCard(
                    titulo = "Massa Muscular",
                    item = analise.analiseMusculo
                )
            }
            item {
                AnaliseInsightCard(
                    titulo = "Hidratação",
                    item = analise.analiseAgua
                )
            }
        }

        item {
            Text(
                text = "Histórico",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        item {
            HistoricoComposicaoComponent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                registros = state.historico
            )
        }
    }
}

@Composable
private fun AnaliseInsightCard(
    titulo: String,
    item: AnaliseItem
) {
    if (item.valor.isBlank()) return

    val statusColor = when (item.status) {
        AnaliseStatus.OTIMO -> Color(0xFF00C853)
        AnaliseStatus.BOM -> Color(0xFF4A90E2)
        AnaliseStatus.BAIXO -> Color(0xFFFDD835)
        AnaliseStatus.ALERTA -> Color(0xFFFF5252)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = item.mensagem,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(50.dp),
                    color = statusColor.copy(alpha = 0.3f),
                    strokeWidth = 4.dp
                )
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.size(50.dp),
                    color = statusColor,
                    strokeWidth = 4.dp,
                )
                Text(
                    text = item.valor,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun ManualEntrySheet(
    viewModel: ComposicaoCorporalViewModel,
    onSave: () -> Unit
) {
    val gordura by viewModel.gorduraPercentual.collectAsState()
    val musculo by viewModel.musculoPercentual.collectAsState()
    val agua by viewModel.aguaPercentual.collectAsState()
    val visceral by viewModel.gorduraVisceral.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Registro Manual",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        FormTextField(
            value = gordura,
            onValueChange = viewModel::onGorduraChange,
            label = "Gordura Corporal (%)"
        )
        FormTextField(
            value = musculo,
            onValueChange = viewModel::onMusculoChange,
            label = "Massa Muscular (%)"
        )
        FormTextField(
            value = agua,
            onValueChange = viewModel::onAguaChange,
            label = "Água Corporal (%)"
        )
        FormTextField(
            value = visceral,
            onValueChange = viewModel::onVisceralChange,
            label = "Gordura Visceral (Nível)"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Salvar Registro")
        }
    }
}

@Composable
private fun EstimativaSheetContent(
    viewModel: ComposicaoCorporalViewModel,
    onSave: () -> Unit
) {
    val altura by viewModel.alturaEstimador.collectAsState()
    val pescoco by viewModel.pescocoEstimador.collectAsState()
    val cintura by viewModel.cinturaEstimador.collectAsState()
    val quadril by viewModel.quadrilEstimador.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Estimativa com Fita",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Icon(
            imageVector = Icons.Default.Straighten,
            contentDescription = "Fita Métrica",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp).align(Alignment.CenterHorizontally)
        )
        Text(
            text = "Use uma fita métrica e insira as medidas em centímetros (cm).",
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        FormTextField(
            value = altura,
            onValueChange = viewModel::onAlturaEstimadorChange,
            label = "Altura (cm)"
        )
        FormTextField(
            value = pescoco,
            onValueChange = viewModel::onPescocoEstimadorChange,
            label = "Pescoço (cm)"
        )
        FormTextField(
            value = cintura,
            onValueChange = viewModel::onCinturaEstimadorChange,
            label = "Cintura (cm)"
        )
        FormTextField(
            value = quadril,
            onValueChange = viewModel::onQuadrilEstimadorChange,
            label = "Quadril (cm) - Apenas se for mulher"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Calcular e Salvar Estimativa")
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
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