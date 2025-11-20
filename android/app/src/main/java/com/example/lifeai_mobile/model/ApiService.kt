package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

// --- MODELS DE AUTENTICAÇÃO (Mantidos) ---
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

// --- NOVOS MODELS PARA O ONBOARDING (Adicionados) ---

/**
 * Passo 1: Dados pessoais enviados para /perfil/
 */
data class PerfilRequest(
    val nome: String,
    val idade: Int,
    val sexo: String,
    val objetivo: String
)

/**
 * Passo 2: Dados corporais enviados para /imc/
 * Backend espera 'imc_res' no JSON, mas no Kotlin podemos chamar de 'imc' usando SerializedName
 */
data class RegistroImcRequest(
    val peso: Double,
    val altura: Double,
    @SerializedName("imc_res")
    val imc: Double,
    val classificacao: String
)