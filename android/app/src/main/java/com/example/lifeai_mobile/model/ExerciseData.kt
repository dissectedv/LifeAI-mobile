package com.example.lifeai_mobile.model

import com.example.lifeai_mobile.R
import com.google.gson.annotations.SerializedName

enum class ImcCategory {
    ABAIXO_DO_PESO,
    PESO_NORMAL,
    SOBREPESO,
    OBESIDADE
}

/**
 * Representa o "Tipo" de exercício (Dados estáticos da UI)
 */
data class Exercise(
    val name: String,
    val imageRes: Int,
    val youtubeUrl: String,
    val idealPara: String,
    val beneficios: List<String>,
    val category: ImcCategory,
    val caloriesBurnedPerMinute: Int
)

/**
 * Representa o objeto enviado para SALVAR um treino (POST)
 */
data class ExerciseSessionRequest(
    @SerializedName("exercise_name") val exerciseName: String,
    @SerializedName("duration_seconds") val durationSeconds: Long,
    @SerializedName("calories_burned") val caloriesBurned: Int,
    @SerializedName("created_at") val createdAt: String
)

/**
 * [NOVO] Representa o objeto recebido do HISTÓRICO (GET)
 * Inclui o ID gerado pelo banco de dados.
 */
data class ExerciseSessionResponse(
    val id: Int,
    @SerializedName("exercise_name") val exerciseName: String,
    @SerializedName("duration_seconds") val durationSeconds: Long,
    @SerializedName("calories_burned") val caloriesBurned: Int,
    @SerializedName("created_at") val createdAt: String
)

object ExerciseRepository {

    fun getExercisesForImc(imc: Float): List<Exercise> {
        val category = when {
            imc < 18.5f -> ImcCategory.ABAIXO_DO_PESO
            imc <= 24.9f -> ImcCategory.PESO_NORMAL
            imc <= 29.9f -> ImcCategory.SOBREPESO
            else -> ImcCategory.OBESIDADE
        }
        return exercises.filter { it.category == category }
    }

    private val exercises = listOf(
        Exercise(
            name = "Agachamento com Barra",
            imageRes = R.drawable.agachamentocombarra,
            youtubeUrl = "https://www.youtube.com/watch?v=4L5nBs8Eq7g&pp=ygUVQWdhY2hhbWVudG8gY29tIEJhcnJh",
            idealPara = "Construir força e massa muscular nas pernas e glúteos.",
            beneficios = listOf("Melhor exercício para pernas.", "Aumenta o metabolismo.", "Fortalece o core."),
            category = ImcCategory.ABAIXO_DO_PESO,
            caloriesBurnedPerMinute = 6
        ),
        Exercise(
            name = "Supino Reto",
            imageRes = R.drawable.supinoreto,
            youtubeUrl = "https://www.youtube.com/watch?v=WwXS2TeFmeg&pp=ygULU3VwaW5vIFJldG8%3D",
            idealPara = "Construir músculos no peito, ombros e tríceps.",
            beneficios = listOf("Principal exercício para peitoral.", "Desenvolve força de empurrar.", "Base para hipertrofia."),
            category = ImcCategory.ABAIXO_DO_PESO,
            caloriesBurnedPerMinute = 5
        ),
        Exercise(
            name = "Remada Curvada",
            imageRes = R.drawable.remadacurvada,
            youtubeUrl = "https://www.youtube.com/watch?v=mxvS-iwm53o&pp=ygUOUmVtYWRhIEN1cnZhZGE%3D",
            idealPara = "Desenvolver costas largas e densas (dorsais).",
            beneficios = listOf("Fortalece a 'puxada'.", "Melhora a postura.", "Trabalha bíceps e antebraços."),
            category = ImcCategory.ABAIXO_DO_PESO,
            caloriesBurnedPerMinute = 6
        ),
        Exercise(
            name = "Flexão de Braço",
            imageRes = R.drawable.flexaodebraco,
            youtubeUrl = "https://www.youtube.com/watch?v=GOj4TMPVuZg&pp=ygURRmxleMOjbyBkZSBCcmHDp28%3D",
            idealPara = "Manutenção da força do peitoral e tríceps com peso corporal.",
            beneficios = listOf("Ótimo para fazer em casa.", "Fortalece o core.", "Melhora resistência muscular."),
            category = ImcCategory.PESO_NORMAL,
            caloriesBurnedPerMinute = 8
        ),
        Exercise(
            name = "Prancha Abdominal",
            imageRes = R.drawable.prancha,
            youtubeUrl = "https://www.youtube.com/watch?v=dduZjSypIS8&pp=ygURUHJhbmNoYSBBYmRvbWluYWzSBwkJAwoBhyohjO8%3D",
            idealPara = "Fortalecimento do core e estabilidade lombar.",
            beneficios = listOf("Define o abdômen.", "Previne dores nas costas.", "Melhora o equilíbrio."),
            category = ImcCategory.PESO_NORMAL,
            caloriesBurnedPerMinute = 4
        ),
        Exercise(
            name = "Corrida Estacionária",
            imageRes = R.drawable.corridaestacionaria,
            youtubeUrl = "https://www.youtube.com/watch?v=pvLUTrZFvi4&pp=ygUVQ29ycmlkYSBFc3RhY2lvbsOhcmlh",
            idealPara = "Exercício cardiovascular leve para manter o condicionamento sem impacto.",
            beneficios = listOf("Melhora o fôlego.", "Aumenta o gasto calórico diário.", "Pode ser feito em qualquer lugar."),
            category = ImcCategory.PESO_NORMAL,
            caloriesBurnedPerMinute = 10
        ),
        Exercise(
            name = "Burpee (Adaptado)",
            imageRes = R.drawable.burpeeadaptado,
            youtubeUrl = "https://www.youtube.com/shorts/nBi4jFkRUmM",
            idealPara = "Alto gasto calórico e condicionamento total do corpo.",
            beneficios = listOf("Trabalha o corpo inteiro.", "Eleva muito a frequência cardíaca.", "Melhora o fôlego (VO2)."),
            category = ImcCategory.SOBREPESO,
            caloriesBurnedPerMinute = 12
        ),
        Exercise(
            name = "Agachamento Goblet",
            imageRes = R.drawable.agachamentogoble,
            youtubeUrl = "https://www.youtube.com/watch?v=ge1vdJRP0UA&pp=ygUSQWdhY2hhbWVudG8gR29ibGV0",
            idealPara = "Queimar calorias e fortalecer pernas sem sobrecarregar as costas.",
            beneficios = listOf("Seguro para iniciantes.", "Fortalece pernas e glúteos.", "Ativa o core."),
            category = ImcCategory.SOBREPESO,
            caloriesBurnedPerMinute = 7
        ),
        Exercise(
            name = "Corrida Leve (Esteira)",
            imageRes = R.drawable.esteiraleve,
            youtubeUrl = "https://www.youtube.com/watch?v=O3f1CpmYfDU&pp=ygUWQ29ycmlkYSBMZXZlIChFc3RlaXJhKQ%3D%3D",
            idealPara = "Melhorar a saúde cardiovascular e auxiliar na queima de gordura.",
            beneficios = listOf("Alto gasto calórico.", "Fortalece o coração.", "Reduz o estresse."),
            category = ImcCategory.SOBREPESO,
            caloriesBurnedPerMinute = 9
        ),
        Exercise(
            name = "Caminhada Rápida",
            imageRes = R.drawable.caminhadarapida,
            youtubeUrl = "https://www.youtube.com/watch?v=MIYuqCn7gKI&pp=ygURQ2FtaW5oYWRhIFLDoXBpZGE%3D",
            idealPara = "Começar a se movimentar, queimar calorias e proteger as articulações.",
            beneficios = listOf("Baixo impacto (seguro).", "Melhora a circulação.", "Controla a pressão arterial."),
            category = ImcCategory.OBESIDADE,
            caloriesBurnedPerMinute = 5
        ),
        Exercise(
            name = "Bicicleta Ergométrica",
            imageRes = R.drawable.bikeergometrica,
            youtubeUrl = "https://www.youtube.com/shorts/zFSTteth0qU",
            idealPara = "Exercício cardiovascular sem impacto nos joelhos.",
            beneficios = listOf("Queima calorias.", "Fortalece as pernas.", "Não prejudica os joelhos."),
            category = ImcCategory.OBESIDADE,
            caloriesBurnedPerMinute = 7
        ),
        Exercise(
            name = "Remo (Máquina)",
            imageRes = R.drawable.remomaquina,
            youtubeUrl = "https://www.youtube.com/shorts/bCxq4zMHpzs",
            idealPara = "Exercício de corpo inteiro com baixo impacto.",
            beneficios = listOf("Trabalha costas, pernas e braços.", "Cardio e força ao mesmo tempo.", "Baixo impacto."),
            category = ImcCategory.OBESIDADE,
            caloriesBurnedPerMinute = 8
        )
    )
}