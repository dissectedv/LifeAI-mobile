package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class PerfilImcBase (
    @SerializedName("nome")
    val nome: String,

    @SerializedName("idade")
    val idade: Int,

    @SerializedName("peso")
    val peso: Float,

    @SerializedName("altura")
    val altura: Float,

    @SerializedName("sexo")
    val sexo: String,

    @SerializedName("objetivo")
    val objetivo: String,

    @SerializedName("imc_res")
    val imcResultado: Float,

    @SerializedName("classificacao")
    val classificacao: String
)