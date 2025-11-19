package com.example.lifeai_mobile.utils

import androidx.compose.ui.graphics.Color
import java.util.Calendar

object HealthUiUtils {

    fun getSaudacao(): String {
        val calendar = Calendar.getInstance()
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> "Bom dia"
            in 12..17 -> "Boa tarde"
            else -> "Boa noite"
        }
    }

    fun getImcColor(imc: Float): Color {
        return when {
            imc < 18.5f -> Color(0xFF4A90E2)
            imc <= 24.9f -> Color(0xFF00C853)
            imc <= 29.9f -> Color(0xFFFDD835)
            else -> Color(0xFFFF5252)
        }
    }

    fun getImcClassificacao(imc: Float): String {
        return when {
            imc < 18.5f -> "ABAIXO DO PESO"
            imc <= 24.9f -> "PESO NORMAL"
            imc <= 29.9f -> "SOBREPESO"
            else -> "OBESIDADE"
        }
    }

    fun getFraseMotivacional(imc: Float): String {
        return when {
            imc < 18.5f -> "Vamos fortalecer sua rotina e alcançar o equilíbrio!"
            imc <= 24.9f -> "Excelente! Continue cuidando da sua saúde."
            imc <= 29.9f -> "Você está quase lá! Continue progredindo."
            else -> "Vamos te ajudar a começar essa mudança!"
        }
    }

    fun calculateImcProgress(imc: Float): Float {
        val minNormalImc = 18.5f
        val maxNormalImc = 24.9f
        if (imc < minNormalImc) return 0.15f
        val range = maxNormalImc - minNormalImc
        return ((imc - minNormalImc) / range).coerceIn(0f, 1f)
    }

    fun getAnaliseGordura(gordura: Float, sexo: String): Pair<String, Color> {
        if (gordura <= 0) return "N/A" to Color.White.copy(alpha = 0.7f)

        val isMale = sexo.equals("Masculino", ignoreCase = true)

        return if (isMale) {
            when {
                gordura < 8 -> "Muito Baixo" to Color(0xFFFDD835)
                gordura <= 20 -> "Ideal" to Color(0xFF00C853)
                gordura <= 25 -> "Saudável" to Color(0xFF4A90E2)
                else -> "Elevado" to Color(0xFFFF5252)
            }
        } else {
            when {
                gordura < 15 -> "Muito Baixo" to Color(0xFFFDD835)
                gordura <= 25 -> "Ideal" to Color(0xFF00C853)
                gordura <= 32 -> "Saudável" to Color(0xFF4A90E2)
                else -> "Elevado" to Color(0xFFFF5252)
            }
        }
    }
}