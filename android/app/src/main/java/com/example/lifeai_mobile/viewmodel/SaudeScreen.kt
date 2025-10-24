package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SaudeScreen(mainNavController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top // <-- MUDANÇA AQUI
    ) {
        // Removido o Column extra que agrupava os itens de cima
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
                onClick = { }
            )
            SaudeCard(
                title = "Planejamento de Rotina",
                description = "Dicas e organização de tarefas diárias.",
                icon = Icons.Default.DateRange,
                onClick = { }
            )
            SaudeCard(
                title = "Sugestão de Dieta",
                description = "Dieta sugerida e personalizável com base no seu perfil.",
                icon = Icons.Default.Restaurant,
                onClick = { }
            )
            SaudeCard(
                title = "Análise de Sono",
                description = "Monitore sua qualidade do sono.",
                icon = Icons.Default.Nightlight,
                onClick = { }
            )

            // <-- CARD DE IMC MOVIDO PARA CÁ
            SaudeCard(
                title = "Calculadora de IMC",
                description = "Calcule seu IMC e acompanhe sua evolução.",
                icon = Icons.Default.MonitorHeart,
                onClick = { mainNavController.navigate("imc_calculator") }
            )
        }

        // <-- CARD DE IMC REMOVIDO DAQUI
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