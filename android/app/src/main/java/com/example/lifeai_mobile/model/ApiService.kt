package com.example.lifeai_mobile.model

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
