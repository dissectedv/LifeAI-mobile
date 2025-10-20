package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName
import java.util.Date

// Dados que enviaremos para POST /imc/
data class ImcRecordRequest(
    @SerializedName("idade")
    val idade: Int,
    @SerializedName("sexo")
    val sexo: String,
    @SerializedName("peso")
    val peso: Float,
    @SerializedName("altura")
    val altura: Float,
    @SerializedName("data_consulta")
    val dataConsulta: String
)

// Dados que recebemos de volta da API
data class ImcRecordResponse(
    @SerializedName("mensagem")
    val mensagem: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("imc")
    val imc: Float,
    @SerializedName("classificacao")
    val classificacao: String
)