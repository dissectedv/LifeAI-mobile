package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// O que enviamos para a API
data class ChatRequest(
    val pergunta: String,

    @SerializedName("sessao_id")
    val sessaoId: String,

    // NOVO CAMPO: Permite forçar a geração de nova dieta ignorando o banco
    @SerializedName("force_new")
    val forceNew: Boolean = false
)

// O que recebemos da API
data class ChatResponse(
    val resposta: String?,
    val erro: String?
)