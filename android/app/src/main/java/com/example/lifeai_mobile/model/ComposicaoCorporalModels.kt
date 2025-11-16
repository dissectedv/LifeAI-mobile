package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class ComposicaoCorporalRegistro(
    @SerializedName("id")
    val id: Int,

    @SerializedName("data_consulta")
    val dataConsulta: String,

    @SerializedName("gordura_percentual")
    val gorduraPercentual: Float,

    @SerializedName("musculo_percentual")
    val musculoPercentual: Float,

    @SerializedName("agua_percentual")
    val aguaPercentual: Float,

    @SerializedName("gordura_visceral")
    val gorduraVisceral: Int,

    @SerializedName("estimado")
    val estimado: Boolean
)

data class ComposicaoCorporalRequest(
    @SerializedName("gordura_percentual")
    val gorduraPercentual: Float,

    @SerializedName("musculo_percentual")
    val musculoPercentual: Float,

    @SerializedName("agua_percentual")
    val aguaPercentual: Float,

    @SerializedName("gordura_visceral")
    val gorduraVisceral: Int,

    @SerializedName("estimado")
    val estimado: Boolean
)