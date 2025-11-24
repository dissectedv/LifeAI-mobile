package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// Usado para leitura (GET)
data class ImcRegistro(
    @SerializedName("id") val id: Int,
    @SerializedName("data_consulta") val dataConsulta: String,
    @SerializedName("peso") val peso: Double,
    @SerializedName("altura") val altura: Double,
    @SerializedName("imc_res") val imcRes: Double,
    @SerializedName("classificacao") val classificacao: String
)

// Usado para envio (POST)
data class RegistroImcRequest(
    @SerializedName("peso")
    val peso: Double,

    @SerializedName("altura")
    val altura: Double,

    @SerializedName("imc")
    val imc: Double,

    @SerializedName("classificacao")
    val classificacao: String,

    // --- OBRIGATÓRIO: Adicione esta linha para o erro sumir ---
    @SerializedName("data_consulta")
    val data: String
)