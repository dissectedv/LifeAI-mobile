package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// Esta classe mapeia EXATAMENTE a resposta JSON da sua API
data class ImcRegistro(

    @SerializedName("id")
    val id: Int,

    @SerializedName("data_consulta")
    val dataConsulta: String, // O DateField do Django vira uma String "YYYY-MM-DD"

    @SerializedName("idade")
    val idade: Int,

    @SerializedName("sexo")
    val sexo: String,

    @SerializedName("peso")
    val peso: Double,

    @SerializedName("altura")
    val altura: Double,

    @SerializedName("imc_res")
    val imcRes: Double, // O 'imc_res' do modelo

    @SerializedName("classificacao")
    val classificacao: String
)