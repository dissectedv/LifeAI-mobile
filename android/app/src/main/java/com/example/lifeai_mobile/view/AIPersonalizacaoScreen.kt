package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.NoFood
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lifeai_mobile.viewmodel.AIPersonalizacaoViewModel
import com.example.lifeai_mobile.viewmodel.PersonalizacaoState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIPersonalizacaoScreen(
    navController: NavController,
    viewModel: AIPersonalizacaoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState) {
        when (uiState) {
            is PersonalizacaoState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Perfil atualizado com sucesso!") }
                viewModel.resetState()
            }
            is PersonalizacaoState.NoChanges -> {
                scope.launch { snackbarHostState.showSnackbar("Nenhuma alteração para salvar.") }
                viewModel.resetState()
            }
            else -> {} // Outros estados não precisam de snackbar automática
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuração da IA", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D1A26))
            )
        },
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding() // Ajuda a subir a tela quando o teclado abre
        ) {
            when (val state = uiState) {
                is PersonalizacaoState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A90E2))
                    }
                }
                is PersonalizacaoState.Error -> {
                    ErrorContent(state.message) { viewModel.carregarDadosAtuais() }
                }
                else -> {
                    ModernFormContent(
                        viewModel = viewModel,
                        isSaving = uiState is PersonalizacaoState.Saving
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
        ) {
            Text("Tentar Novamente")
        }
    }
}

@Composable
private fun ModernFormContent(
    viewModel: AIPersonalizacaoViewModel,
    isSaving: Boolean
) {
    val nome by viewModel.nome.collectAsState()
    val idade by viewModel.idade.collectAsState()
    val peso by viewModel.peso.collectAsState()
    val altura by viewModel.altura.collectAsState()
    val sexo by viewModel.sexo.collectAsState()
    val objetivo by viewModel.objetivo.collectAsState()
    val restricoes by viewModel.restricoes.collectAsState()
    val observacoes by viewModel.observacoes.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(modifier = Modifier.padding(top = 8.dp)) {
            Text(
                text = "Perfil Biométrico & IA",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Mantenha seus dados atualizados para a IA calcular suas métricas corretamente.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        SectionGroup("Identidade") {
            ModernTextField(
                value = nome,
                onValueChange = { viewModel.nome.value = it },
                label = "Nome ou Apelido",
                icon = Icons.Default.Person,
                enabled = !isSaving
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModernTextField(
                    value = sexo,
                    onValueChange = {},
                    label = "Sexo",
                    icon = Icons.Default.Face,
                    modifier = Modifier.weight(1f),
                    enabled = false // Campo read-only
                )
                ModernTextField(
                    value = idade,
                    onValueChange = { viewModel.idade.value = it },
                    label = "Idade",
                    icon = Icons.Default.Cake,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
            }
        }

        SectionGroup("Medidas Corporais") {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModernTextField(
                    value = peso,
                    onValueChange = { viewModel.peso.value = it },
                    label = "Peso (kg)",
                    icon = Icons.Default.MonitorWeight,
                    keyboardType = KeyboardType.Decimal, // Permite vírgula/ponto
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
                ModernTextField(
                    value = altura,
                    onValueChange = { viewModel.altura.value = it },
                    label = "Altura (cm ou m)", // Deixa claro para o user
                    icon = Icons.Default.Height,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving
                )
            }
        }

        SectionGroup("Calibragem da IA") {
            ModernTextField(
                value = objetivo,
                onValueChange = { viewModel.objetivo.value = it },
                label = "Objetivo Principal",
                icon = Icons.Default.FitnessCenter,
                enabled = !isSaving
            )
            ModernTextField(
                value = restricoes,
                onValueChange = { viewModel.restricoes.value = it },
                label = "Restrições Alimentares",
                icon = Icons.Default.NoFood,
                enabled = !isSaving
            )
            ModernTextField(
                value = observacoes,
                onValueChange = { viewModel.observacoes.value = it },
                label = "Histórico de Saúde",
                icon = Icons.Default.Healing,
                enabled = !isSaving
            )
        }

        SaveButton(isSaving = isSaving) { viewModel.salvarAlteracoes() }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun SectionGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFF4A90E2),
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content
            )
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = {
            if (placeholder.isNotEmpty()) Text(placeholder, color = Color.White.copy(0.3f))
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color(0xFF4A90E2) else Color.Gray
            )
        },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4A90E2),
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedLabelColor = Color(0xFF4A90E2),
            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF4A90E2),
            focusedContainerColor = Color(0xFF0D1A26).copy(alpha = 0.5f),
            unfocusedContainerColor = Color(0xFF0D1A26).copy(alpha = 0.5f)
        )
    )
}

@Composable
fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFF00C9A7), Color(0xFF009E83))
    )

    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFF00C9A7).copy(alpha = 0.5f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (!isSaving) gradientBrush else Brush.linearGradient(listOf(Color.Gray, Color.Gray))),
            contentAlignment = Alignment.Center
        ) {
            if (isSaving) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Salvando...", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Salvar Alterações", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}