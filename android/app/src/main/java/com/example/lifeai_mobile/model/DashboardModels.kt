package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class ImcBaseProfile(
    @SerializedName("nome")
    val nome: String,
    @SerializedName("idade")
    val idade: Int,
    @SerializedName("peso")
    val peso: Double,
    @SerializedName("altura")
    val altura: Double,
    @SerializedName("sexo")
    val sexo: String,
    @SerializedName("imc_res")
    val imcResultado: Double,
    @SerializedName("classificacao")
    val classificacao: String?,
    @SerializedName("objetivo")
    val objetivo: String
)