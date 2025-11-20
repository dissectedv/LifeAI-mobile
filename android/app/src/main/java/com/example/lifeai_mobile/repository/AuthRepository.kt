package com.example.lifeai_mobile.repository

import android.util.Log
import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.ChatResponse
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.ComposicaoCorporalRequest
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.LogoutRequest
import com.example.lifeai_mobile.model.PerfilRequest
import com.example.lifeai_mobile.model.PerfilResponse // <--- NOVO IMPORT
import com.example.lifeai_mobile.model.RegistroImcRequest
import com.example.lifeai_mobile.model.RefreshTokenRequest
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.HttpException
import com.example.lifeai_mobile.utils.SessionManager
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) {
    suspend fun registerUser(
        username: String,
        email: String,
        password: String
    ): Response<RegisterResponse> {
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

    // --- CRIAÇÃO DE PERFIL EM DUAS ETAPAS ---
    suspend fun createFullProfile(perfil: PerfilRequest, imc: RegistroImcRequest): Response<Unit> {
        val perfilResponse = api.createProfile(perfil)
        if (!perfilResponse.isSuccessful) {
            return perfilResponse
        }
        return api.createImcRecord(imc)
    }

    suspend fun createImcRecord(record: RegistroImcRequest): Response<Unit> {
        return api.createImcRecord(record)
    }
    // -----------------------------------------------------

    // --- GESTÃO DE PERFIL (ATUALIZADO) ---

    suspend fun getProfileData(): Response<PerfilResponse> {
        return api.getProfileData()
    }

    suspend fun updateProfileData(data: PerfilRequest): Response<PerfilResponse> {
        return api.updateProfileData(data)
    }

    // -------------------------------------

    suspend fun postChatMessage(request: ChatRequest): Response<ChatResponse> {
        return api.postChatMessage(request)
    }

    suspend fun postDietaRequest(request: ChatRequest): Response<DietaResponse> {
        return api.postDietaRequest(request)
    }

    suspend fun getHistoricoImc(): Response<List<ImcRegistro>> {
        return api.getHistoricoImc()
    }

    suspend fun deleteImcRegistro(id: Int) {
        val response = api.deleteImcRegistro(id)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    suspend fun getCompromissos(): Response<List<Compromisso>> {
        return api.getCompromissos()
    }

    suspend fun createCompromisso(compromisso: Compromisso): Response<Compromisso> {
        return api.createCompromisso(compromisso)
    }

    suspend fun updateCompromisso(compromisso: Compromisso): Response<Compromisso> {
        val id = compromisso.id ?: throw Exception("ID inválido")
        return api.updateCompromisso(id, compromisso)
    }

    suspend fun deleteCompromisso(id: Int): Response<Unit> {
        return api.deleteCompromisso(id)
    }

    suspend fun getHistoricoComposicao(): Response<List<ComposicaoCorporalRegistro>> {
        return api.getHistoricoComposicao()
    }

    suspend fun createComposicaoRecord(record: ComposicaoCorporalRequest): Response<ComposicaoCorporalRegistro> {
        return api.createComposicaoRecord(record)
    }
}