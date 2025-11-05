package com.example.lifeai_mobile.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifeai_mobile.viewmodel.DietaState
import com.example.lifeai_mobile.viewmodel.DietaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietaScreen(
    navController: NavController,
    viewModel: DietaViewModel,
    modifier: Modifier = Modifier
) {
    // 1. Coleta o estado do ViewModel (Loading, Success, Error)
    val state by viewModel.state.collectAsState()

    // 2. Chama a função do ViewModel UMA VEZ quando a tela abre
    LaunchedEffect(Unit) {
        viewModel.gerarPlanoDeDieta()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Plano de Dieta IA", fontWeight = FontWeight.Bold, color = Color.White)
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

        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            // 3. Renderiza a UI com base no estado
            when (val currentState = state) {
                is DietaState.Idle, DietaState.Loading -> {
                    // --- ESTADO DE CARREGAMENTO ---
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF4A90E2),
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Analisando seu perfil...",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                        Text(
                            "Estamos gerando seu plano de dieta com IA...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                is DietaState.Error -> {
                    // --- ESTADO DE ERRO ---
                    // (Por agora, vai mostrar o PROMPT aqui, como configuramos)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
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
                            "Erro ao Gerar Plano",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            currentState.message, // A mensagem de erro (ou o prompt)
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                is DietaState.Success -> {
                    // --- ESTADO DE SUCESSO ---
                    // TODO: Implementar a UI de Sucesso (Tabs e Cards)
                    Text(
                        "Plano gerado com sucesso! (Implementar UI aqui)",
                        color = Color.White
                    )
                    // (Vamos implementar a UI de Tabs e Cards aqui no próximo passo)
                }
            }
        }
    }
}