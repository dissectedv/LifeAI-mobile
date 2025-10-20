package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lifeai_mobile.components.CalculatorImcCard

@Composable
fun SaudeScreen(mainNavController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Saúde e Bem-estar",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

       CalculatorImcCard(
            title = "Calcular Novo IMC",
            description = "Registre seu peso e altura para acompanhar sua evolução.",
            icon = Icons.Default.MonitorHeart,
            onClick = {
                mainNavController.navigate("imc_calculator")
            }
        )

        // Você pode adicionar mais cards ou outros componentes aqui no futuro
    }
}