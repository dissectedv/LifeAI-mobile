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
    // Para a demo, vamos usar um ID aleatório (UUID)
    // para que a LazyColumn (a lista) consiga diferenciar os itens.
    val id: String = UUID.randomUUID().toString(),
    val titulo: String,
    val data: String, // "YYYY-MM-DD"
    val hora_inicio: String, // "HH:MM"
    val hora_fim: String, // "HH:MM"
    val concluido: Boolean = false
)