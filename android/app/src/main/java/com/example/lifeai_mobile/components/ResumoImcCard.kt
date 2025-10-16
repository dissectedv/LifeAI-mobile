package com.example.lifeai_mobile.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.viewmodel.ResumoViewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ResumoImcCard(
    profile: ImcBaseProfile,
    viewModel: ResumoViewModel,
    modifier: Modifier = Modifier
) {
    val imc = profile.imcResultado
    val posicaoBarra = viewModel.calcularPosicao(imc)

    val score = viewModel.calcularScore(imc)
    val scoreDescription = viewModel.getScoreDescription(score)

    val progressBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF3B82F6), // Azul
            Color(0xFF10B981), // Verde
            Color(0xFFFACC15), // Amarelo
            Color(0xFFF97316), // Laranja
            Color(0xFFEF4444)  // Vermelho
        )
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Resumo IMC",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = viewModel.obterMensagemIMC(profile.imcResultado),
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1.5f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoRow(label = "Peso:", value = "${profile.peso} kg")
                    InfoRow(label = "Altura:", value = "${profile.altura} m")
                    InfoRow(label = "Objetivo:", value = profile.objetivo)
                }

                Spacer(modifier = Modifier.width(16.dp))

                ScoreIndicator(
                    score = score,
                    maxScore = 10,
                    label = scoreDescription, // Usa a descrição calculada
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            ) {
                // A barra colorida de fundo
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(6.dp))
                        .background(progressBrush)
                )

                val maxWidthInDp = with(LocalDensity.current) { constraints.maxWidth.toDp() }
                val markerOffset = (maxWidthInDp * posicaoBarra) - 6.dp

                Spacer(
                    modifier = Modifier
                        .offset(x = markerOffset)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Gray, CircleShape) // Borda sutil para destaque
                )
            }
        }
    }
}


@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ScoreIndicator(score: Int, maxScore: Int, label: String?, modifier: Modifier = Modifier) {
    val progress = score.toFloat() / maxScore.toFloat()
    val indicatorColor = Color(0xFFFACC15)

    Box(
        modifier = modifier.size(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = indicatorColor.copy(alpha = 0.3f),
            strokeWidth = 8.dp
        )
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize(),
            color = indicatorColor,
            strokeWidth = 8.dp
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score/$maxScore",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!label.isNullOrBlank()) {
                Text(
                    text = label.uppercase(),
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}