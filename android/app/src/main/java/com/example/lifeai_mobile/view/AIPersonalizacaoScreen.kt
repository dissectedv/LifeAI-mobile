package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
        if (uiState is PersonalizacaoState.Success) {
            scope.launch {
                snackbarHostState.showSnackbar("Perfil atualizado com sucesso!")
            }
            viewModel.resetState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personalizar IA",
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
        containerColor = Color(0xFF0D1A26)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is PersonalizacaoState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF4A90E2))
                    }
                }
                is PersonalizacaoState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { viewModel.carregarDadosAtuais() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A90E2))
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }
                else -> {
                    PersonalizacaoForm(
                        viewModel = viewModel,
                        isSaving = uiState is PersonalizacaoState.Saving
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonalizacaoForm(
    viewModel: AIPersonalizacaoViewModel,
    isSaving: Boolean
) {
    val nome by viewModel.nome.collectAsState()
    val idade by viewModel.idade.collectAsState()
    val peso by viewModel.peso.collectAsState()
    val altura by viewModel.altura.collectAsState()
    val sexo by viewModel.sexo.collectAsState()
    val objetivo by viewModel.objetivo.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Dados para a Inteligência Artificial",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Mantenha seus dados atualizados para que a IA possa gerar dietas e treinos precisos para o seu momento atual.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        CustomTextField(
            value = nome,
            onValueChange = { viewModel.nome.value = it },
            label = "Nome ou Apelido",
            enabled = !isSaving
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CustomTextField(
                value = idade,
                onValueChange = { viewModel.idade.value = it },
                label = "Idade",
                keyboardType = KeyboardType.Number,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            )
            CustomTextField(
                value = sexo,
                onValueChange = {},
                label = "Sexo Biológico",
                readOnly = true,
                modifier = Modifier.weight(1f),
                enabled = false
            )
        }

        GenderSelectionRow(
            selectedGender = sexo,
            onGenderSelected = { viewModel.sexo.value = it },
            enabled = !isSaving
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CustomTextField(
                value = peso,
                onValueChange = { viewModel.peso.value = it },
                label = "Peso (kg)",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            )
            CustomTextField(
                value = altura,
                onValueChange = { viewModel.altura.value = it },
                label = "Altura (m)",
                keyboardType = KeyboardType.Decimal,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            )
        }

        CustomTextField(
            value = objetivo,
            onValueChange = { viewModel.objetivo.value = it },
            label = "Seu Objetivo Principal",
            placeholder = "Ex: Ganhar massa muscular, Perder gordura...",
            singleLine = false,
            maxLines = 3,
            enabled = !isSaving
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.salvarAlteracoes() },
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00C9A7),
                disabledContainerColor = Color(0xFF00C9A7).copy(alpha = 0.5f)
            )
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Salvando...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salvar Alterações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GenderSelectionRow(
    selectedGender: String,
    onGenderSelected: (String) -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GenderOption(
            text = "Masculino",
            isSelected = selectedGender.equals("Masculino", ignoreCase = true),
            onClick = { onGenderSelected("Masculino") },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
        GenderOption(
            text = "Feminino",
            isSelected = selectedGender.equals("Feminino", ignoreCase = true),
            onClick = { onGenderSelected("Feminino") },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
    }
}

@Composable
private fun GenderOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean
) {
    val borderColor = if (isSelected) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.3f)
    val backgroundColor = if (isSelected) Color(0xFF4A90E2).copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) Color(0xFF4A90E2) else Color.White.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = Color.White.copy(alpha = 0.4f)) },
        modifier = modifier.fillMaxWidth(),
        readOnly = readOnly,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4A90E2),
            focusedLabelColor = Color(0xFF4A90E2),
            unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
            unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
            cursorColor = Color(0xFF4A90E2),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White.copy(alpha = 0.5f),
            disabledBorderColor = Color.White.copy(alpha = 0.2f),
            disabledLabelColor = Color.White.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(12.dp)
    )
}