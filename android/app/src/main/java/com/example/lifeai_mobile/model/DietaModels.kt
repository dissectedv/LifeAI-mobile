package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class DietaResponse(
    @SerializedName("plano_diario")
    val planoDiario: List<PlanoDiario>
)

data class PlanoDiario(
    @SerializedName("dia")
    val dia: String,
    @SerializedName("resumo_kcal")
    val resumoKcal: Int,
    @SerializedName("macros")
    val macros: Macros,
    @SerializedName("refeicoes")
    val refeicoes: List<Refeicao>
)

data class Macros(
    @SerializedName("proteina_g")
    val proteina: Int,
    @SerializedName("carbo_g")
    val carbo: Int,
    @SerializedName("gordura_g")
    val gordura: Int
)

data class Refeicao(
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("opcoes_acessiveis")
    val opcoesAcessiveis: List<String>,
    @SerializedName("opcoes_ideais")
    val opcoesIdeais: List<String>
)