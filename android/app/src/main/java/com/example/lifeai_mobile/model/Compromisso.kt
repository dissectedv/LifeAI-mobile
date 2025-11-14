package com.example.lifeai_mobile.model

data class Compromisso(
    val id: Int? = null,
    val titulo: String,
    val data: String,          // formato: "YYYY-MM-DD"
    val hora_inicio: String,   // formato: "HH:MM"
    val hora_fim: String,      // formato: "HH:MM"
    val concluido: Boolean = false
)