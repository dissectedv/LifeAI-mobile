package com.example.lifeai_mobile.viewmodel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.view.ResumoViewModelFactory
import java.util.Locale

@Composable
fun HomeScreen(resumoViewModelFactory: ResumoViewModelFactory) {
    val viewModel: ResumoViewModel = viewModel(factory = resumoViewModelFactory)
    val state by viewModel.state.collectAsState()

    // 1. Column principal (NÃO rolável) que define o fundo
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1A26))
    ) {
        // 2. O seu texto, agora fixo no topo
        Text(
            text = "LifeAI", // <-- MUDANÇA DE TEXTO
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp) // <-- Adiciona o padding só para o texto
        )

        // 3. Column interna (ROLÁVEL) com o resto do conteúdo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // <-- SCROLL APLICADO AQUI
                .padding(horizontal = 16.dp), // <-- Padding horizontal para os cards
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Todo o seu conteúdo (exceto o texto do título) vai aqui
            ImcHistoricoCard()

            when (val currentState = state) {
                is ResumoState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is ResumoState.Error -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Erro ao carregar resumo: ${currentState.message}",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                is ResumoState.Success -> {
                    ResumoImcCard(profile = currentState.profile)
                }
            }

            GerarDicaIAButton()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PassosCard(modifier = Modifier.weight(1f))
                AtividadesCard(modifier = Modifier.weight(1f))
            }

            // Adiciona um espaço no final para o último card não colar na BottomBar
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

//
// O restante do seu código (GerarDicaIAButton, ResumoImcCard, etc.)
// permanece exatamente igual.
//

@Composable
private fun GerarDicaIAButton() {
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF007BFF), Color(0xFF6C63FF))
    )

    Button(
        onClick = { /* TODO: Ação de gerar dica */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .background(gradient, shape = RoundedCornerShape(16.dp))
                .fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Gerar dica por IA",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "IA",
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumoImcCard(profile: ImcBaseProfile) {
    val imc = profile.imcResultado.toFloat()
    val imcProgress = calculateIdealRangeProgress(imc)

    val progressActiveColor = when {
        imc < 18.5f -> Color(0xFF4A90E2)
        imc <= 24.9f -> Color(0xFF00C853)
        imc <= 29.9f -> Color(0xFFFDD835)
        else -> Color(0xFFFF5252)
    }

    val progressInactiveColor = Color(0xFF2E4A5C)
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    val textColor = Color.White
    val textMutedColor = Color.White.copy(alpha = 0.7f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Seu IMC Atual",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = progressActiveColor.copy(alpha = 0.2f),
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text(
                            text = profile.classificacao?.uppercase(Locale.getDefault()) ?: "N/A",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = progressActiveColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = motivationalPhrase(imc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textMutedColor
                    )
                }

                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutChart(
                        progress = imcProgress,
                        progressColor = progressActiveColor,
                        baseColor = progressInactiveColor,
                        strokeWidth = 22f
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.1f", imc),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "IMC",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textMutedColor
                        )
                    }
                }
            }
        }
    }
}

private fun motivationalPhrase(imc: Float): String {
    return when {
        imc < 18.5f -> "Vamos fortalecer sua rotina e alcançar o equilíbrio!"
        imc <= 24.9f -> "Excelente! Continue cuidando da sua saúde."
        imc <= 29.9f -> "Você está quase lá! Continue progredindo."
        else -> "Vamos te ajudar a começar essa mudança!"
    }
}

private fun calculateIdealRangeProgress(imc: Float): Float {
    val minNormalImc = 18.5f
    val maxNormalImc = 24.9f
    val range = maxNormalImc - minNormalImc
    val progressInBuffer = imc - minNormalImc
    return (progressInBuffer / range).coerceIn(0f, 1f)
}

@Composable
private fun DonutChart(
    progress: Float,
    progressColor: Color,
    baseColor: Color,
    strokeWidth: Float = 28f
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val startAngle = -90f
        val sweepAngleBase = 360f
        val sweepAngleProgress = 360f * progress
        drawArc(
            color = baseColor.copy(alpha = 0.3f),
            startAngle = startAngle,
            sweepAngle = sweepAngleBase,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            brush = Brush.linearGradient(
                colors = listOf(progressColor.copy(alpha = 0.8f), progressColor)
            ),
            startAngle = startAngle,
            sweepAngle = sweepAngleProgress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun ImcHistoricoCard() {
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay),
            contentAlignment = Alignment.Center
        ) {
            Text("Placeholder: IMC Histórico", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun PassosCard(modifier: Modifier = Modifier) {
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    Card(
        modifier = modifier.height(170.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay),
            contentAlignment = Alignment.Center
        ) {
            Text("Placeholder: Passos", color = Color.White.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun AtividadesCard(modifier: Modifier = Modifier) {
    val gradientOverlay = Brush.verticalGradient(
        listOf(Color(0x334A90E2), Color.Transparent)
    )
    Card(
        modifier = modifier.height(170.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2A3D)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientOverlay),
            contentAlignment = Alignment.Center
        ) {
            Text("Placeholder: Atividades", color = Color.White.copy(alpha = 0.7f))
        }
    }
}