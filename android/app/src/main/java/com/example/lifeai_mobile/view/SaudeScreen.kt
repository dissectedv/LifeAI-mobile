package com.example.lifeai_mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// --- IMPORTANTE: AQUI ESTÃO OS IMPORTS (E NÃO AS DEFINIÇÕES) ---
import com.example.lifeai_mobile.viewmodel.ResumoViewModel
import com.example.lifeai_mobile.viewmodel.ResumoState
// ---------------------------------------------------------------

@Composable
fun SaudeScreen(
    mainNavController: NavController,
    navController: NavController,
    resumoViewModel: ResumoViewModel,
    modifier: Modifier = Modifier
) {
    val state by resumoViewModel.state.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            "Saúde e Bem-estar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.shadow(2.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SaudeCard(
                title = "Atividade Física",
                description = "Exercícios físicos personalizados com base no seu perfil.",
                icon = Icons.Default.FitnessCenter,
                onClick = {
                    if (state is ResumoState.Success) {
                        // AQUI ESTÁ A FORMA CORRETA DE ACESSAR O NOVO ESTADO
                        val successState = state as ResumoState.Success
                        val imc = successState.ultimoImc?.imcRes?.toFloat() ?: 0f
                        navController.navigate("atividade_fisica/$imc")
                    }
                }
            )
            SaudeCard(
                title = "Planejamento de Rotina",
                description = "Dicas e organização de tarefas diárias.",
                icon = Icons.Default.DateRange,
                onClick = { navController.navigate("rotina_screen") }
            )
            SaudeCard(
                title = "Sugestão de Dieta",
                description = "Dieta sugerida e personalizável com base no seu perfil.",
                icon = Icons.Default.Restaurant,
                onClick = {
                    navController.navigate("dieta_screen")
                }
            )
            SaudeCard(
                title = "Calculadora de IMC",
                description = "Calcule seu IMC e acompanhe sua evolução.",
                icon = Icons.Default.MonitorHeart,
                onClick = { mainNavController.navigate("imc_calculator") }
            )
            SaudeCard(
                title = "Composição Corporal",
                description = "Analise sua bioimpedância ou estime sua gordura corporal.",
                icon = Icons.Filled.Scale,
                onClick = { mainNavController.navigate("composicao_corporal_screen") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaudeCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val cardBackgroundColor = Color(0xFF1B2A3D)
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    val textColor = Color.White
    val textMutedColor = Color.White.copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = textColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(18.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedColor,
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Acessar",
                    tint = textMutedColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}