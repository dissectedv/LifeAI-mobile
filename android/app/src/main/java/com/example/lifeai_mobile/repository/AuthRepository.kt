package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("registro/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}

class AuthRepository(private val api: AuthApi) {

    suspend fun registerUser(username: String, email: String, password: String): Response<RegisterResponse> {
        val request = RegisterRequest(username, email, password)
        return api.register(request)
    }
}