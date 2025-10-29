package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// O que enviamos para a API
data class ChatRequest(
    val pergunta: String,
    @SerializedName("sessao_id")
    val sessaoId: String
)

// O que recebemos da API
data class ChatResponse(
    val resposta: String?,
    val erro: String?
)