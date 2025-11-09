package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.ChatResponse
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.model.ImcRecordRequest
import com.example.lifeai_mobile.model.ImcRecordResponse
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.LogoutRequest
import com.example.lifeai_mobile.model.PerfilImcBase
import com.example.lifeai_mobile.model.RefreshTokenRequest
import com.example.lifeai_mobile.model.RefreshTokenResponse
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("registro/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("imc_base_perfil/")
    suspend fun imcBase(@Body request: PerfilImcBase): Response<Unit>

    @GET("imc_base_dashboard/")
    suspend fun getImcBaseDashboard(): Response<List<ImcBaseProfile>>

    @POST("imc/")
    suspend fun createImcRecord(@Body request: ImcRecordRequest): Response<ImcRecordResponse>

    @POST("chat-ia/")
    suspend fun postChatMessage(@Body request: ChatRequest): Response<ChatResponse>

    @POST("gerar-dieta-ia/")
    suspend fun postDietaRequest(@Body request: ChatRequest): Response<DietaResponse>

    @GET("imc/registrosConsultas/")
    suspend fun getHistoricoImc(): List<ImcRegistro>

    @DELETE("imc/{id}/")
    suspend fun deleteImcRegistro(@Path("id") id: Int): Response<Unit>

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("logout/")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    // --- CORREÇÃO: Removido o "api/" do path ---
    @POST("send-email/")
    suspend fun sendEmail(@Body emailData: Map<String, String>): Response<Unit>
}