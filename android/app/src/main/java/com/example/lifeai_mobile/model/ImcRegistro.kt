package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class ImcRegistro(
    @SerializedName("id")
    val id: Int,

    @SerializedName("data_consulta")
    val dataConsulta: String,

    @SerializedName("peso")
    val peso: Double,

    @SerializedName("altura")
    val altura: Double,

    @SerializedName("imc_res")
    val imcRes: Double,

    @SerializedName("classificacao")
    val classificacao: String
)