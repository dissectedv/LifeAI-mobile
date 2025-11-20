package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// --- MODELS DE AUTENTICAÇÃO ---
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val message: String,
    val refresh: String?,
    val access: String?,
    val user: UserData?
)

data class UserData(
    val id: Int,
    val username: String,
    val email: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    val message: String,
    val refresh: String?,
    val access: String?,
    val user: UserData?,

    @SerializedName("onboarding_completed")
    val onboardingCompleted: Boolean
)

// --- MODELS DE PERFIL E SAÚDE ---

/**
 * Usado para ENVIAR dados (POST /perfil/)
 * Adicionado campo de restrições/preferências alimentares
 */
data class PerfilRequest(
    val nome: String,
    val idade: Int,
    val sexo: String,
    val objetivo: String,
    @SerializedName("restricoes_alimentares")
    val restricoesAlimentares: String? = null
)

/**
 * Usado para RECEBER dados (GET /perfil/)
 */
data class PerfilResponse(
    val id: Int,
    val nome: String,
    val sexo: String,
    val idade: Int,
    val objetivo: String,
    @SerializedName("restricoes_alimentares")
    val restricoesAlimentares: String?
)

/**
 * Usado para ENVIAR e RECEBER registro de IMC (POST/GET /imc/)
 */
data class RegistroImcRequest(
    val peso: Double,
    val altura: Double,
    @SerializedName("imc_res")
    val imc: Double,
    val classificacao: String
)