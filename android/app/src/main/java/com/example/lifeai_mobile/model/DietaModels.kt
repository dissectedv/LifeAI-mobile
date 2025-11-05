package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

/**
 * Este é o objeto de nível superior que o Gemini retornará.
 * Ele contém uma lista de planos, um para cada dia.
 */
data class DietaResponse(
    @SerializedName("plano_diario")
    val planoDiario: List<PlanoDiario>
)

/**
 * Representa o plano de um único dia.
 */
data class PlanoDiario(
    @SerializedName("dia")
    val dia: String, // Ex: "Segunda"

    @SerializedName("resumo_kcal")
    val resumoKcal: Int, // Ex: 2100

    @SerializedName("macros")
    val macros: Macros,

    @SerializedName("refeicoes")
    val refeicoes: List<Refeicao>
)

/**
 * Detalhes dos macronutrientes para o dia.
 */
data class Macros(
    @SerializedName("proteina_g")
    val proteina: Int,

    @SerializedName("carbo_g")
    val carbo: Int,

    @SerializedName("gordura_g")
    val gordura: Int
)

/**
 * Representa uma refeição específica (ex: Café da Manhã)
 * com uma ou mais opções.
 */
data class Refeicao(
    @SerializedName("titulo") // Ex: "Café da Manhã"
    val titulo: String,

    @SerializedName("opcoes") // Ex: ["Opção 1: Ovos mexidos...", "Opção 2: Iogurte..."]
    val opcoes: List<String>
)