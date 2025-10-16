package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.PerfilImcBase
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

class AuthRepository(private val api: AuthApi) {

    suspend fun registerUser(username: String, email: String, password: String): Response<RegisterResponse> {
        val request = RegisterRequest(username, email, password)
        return api.register(request)
    }

    // ADICIONE ESTA NOVA FUNÇÃO
    suspend fun loginUser(email: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(email, password)
        return api.login(request)
    }

    suspend fun imcBase(profileData: PerfilImcBase): Response<Unit> {
        return api.imcBase(profileData)
    }

    suspend fun getImcBaseDashboard(): Response<List<ImcBaseProfile>> {
        return api.getImcBaseDashboard()
    }
}