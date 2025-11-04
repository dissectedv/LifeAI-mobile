package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.ChatResponse
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.model.ImcRecordRequest
import com.example.lifeai_mobile.model.ImcRecordResponse
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.PerfilImcBase
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.HttpException
import com.example.lifeai_mobile.utils.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import com.example.lifeai_mobile.model.RefreshTokenRequest
import com.example.lifeai_mobile.model.LogoutRequest

class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {

    suspend fun registerUser(username: String, email: String, password: String): Response<RegisterResponse> {
        val request = RegisterRequest(username, email, password)
        return api.register(request)
    }

    suspend fun loginUser(email: String, password: String): Response<LoginResponse> {
        val request = LoginRequest(email, password)
        val response = api.login(request)

        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.access != null && body.refresh != null) {
                sessionManager.saveTokens(body.access, body.refresh)
            }
        }
        return response
    }

    suspend fun refreshToken(): Boolean {
        val currentRefreshToken = sessionManager.refreshToken.firstOrNull() ?: return false

        return try {
            val request = RefreshTokenRequest(currentRefreshToken)
            val response = api.refreshToken(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    sessionManager.saveTokens(body.access, currentRefreshToken)
                    true
                } else {
                    false
                }
            } else {
                sessionManager.clearTokens()
                false
            }
        } catch (e: Exception) {
            sessionManager.clearTokens()
            false
        }
    }

    suspend fun logoutUser() {
        val currentRefreshToken = sessionManager.refreshToken.firstOrNull()
        if (currentRefreshToken != null) {
            try {
                val request = LogoutRequest(currentRefreshToken)
                api.logout(request)
            } catch (e: Exception) {
            }
        }
        sessionManager.clearTokens()
    }

    suspend fun imcBase(profileData: PerfilImcBase): Response<Unit> {
        return api.imcBase(profileData)
    }

    suspend fun getImcBaseDashboard(): Response<List<ImcBaseProfile>> {
        return api.getImcBaseDashboard()
    }

    suspend fun createImcRecord(record: ImcRecordRequest): Response<ImcRecordResponse> {
        return api.createImcRecord(record)
    }

    suspend fun postChatMessage(request: ChatRequest): Response<ChatResponse> {
        return api.postChatMessage(request)
    }

    suspend fun getHistoricoImc(): List<ImcRegistro> {
        return api.getHistoricoImc()
    }

    suspend fun deleteImcRegistro(id: Int) {
        val response = api.deleteImcRegistro(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }
}