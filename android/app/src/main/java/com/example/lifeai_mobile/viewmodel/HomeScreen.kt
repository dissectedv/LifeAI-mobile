package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifeai_mobile.components.ResumoImcCard
import com.example.lifeai_mobile.view.ResumoViewModelFactory

@Composable
fun HomeScreen(resumoViewModelFactory: ResumoViewModelFactory) {

    val viewModel: ResumoViewModel = viewModel(factory = resumoViewModelFactory)
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Olá!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lógica para exibir o card, loading ou erro
        when (val currentState = state) {
            is ResumoState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ResumoState.Error -> {
                Text(
                    "Erro ao carregar resumo: ${currentState.message}",
                    color = MaterialTheme.colorScheme.error
                )
            }
            is ResumoState.Success -> {
                ResumoImcCard(
                    profile = currentState.profile,
                    viewModel = viewModel
                )
            }
        }
    }
}