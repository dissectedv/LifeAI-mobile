package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class ImcGraficoData(
    @SerializedName("imcRes")
    val imcRes: Double? = null,

    // algumas APIs usam "imc" em vez de "imcRes" — deixamos os dois por segurança
    @SerializedName("imc")
    val imc: Double? = null,

    // data pode vir como "dataConsulta" ou "data"
    @SerializedName("dataConsulta")
    val dataConsulta: String? = null,

    @SerializedName("data")
    val data: String? = null
) {
    // Retorna o valor de IMC como Float, priorizando imcRes -> imc -> 0.0
    fun imcValue(): Float = ((imcRes ?: imc ?: 0.0)).toFloat()

    // Retorna a string de data preferida (prioriza dataConsulta -> data -> "")
    fun dataValue(): String = dataConsulta ?: data ?: ""
}
