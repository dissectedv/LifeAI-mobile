package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

data class PerfilImcBase (
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

    @SerializedName("objetivo")
    val objetivo: String
)