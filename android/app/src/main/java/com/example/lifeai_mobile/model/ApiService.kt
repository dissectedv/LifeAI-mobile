package com.example.lifeai_mobile.model

import com.google.gson.annotations.SerializedName

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
