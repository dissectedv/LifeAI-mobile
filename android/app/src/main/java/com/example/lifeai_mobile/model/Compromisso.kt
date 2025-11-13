package com.example.lifeai_mobile.model

import java.util.UUID

/*
Este é o "molde" (data class) para o nosso compromisso.
Por enquanto, ele não precisa de anotações de API (@SerializedName)
porque estamos usando uma lista em memória para a demo.

// TODO: Quando o backend estiver pronto, adicionar as anotações
// @SerializedName("hora_inicio") para bater com a API.
*/

data class Compromisso(
    val id: Int? = null,
    val titulo: String,
    val data: String,          // formato: "YYYY-MM-DD"
    val hora_inicio: String,   // formato: "HH:MM"
    val hora_fim: String,      // formato: "HH:MM"
    val concluido: Boolean = false
)