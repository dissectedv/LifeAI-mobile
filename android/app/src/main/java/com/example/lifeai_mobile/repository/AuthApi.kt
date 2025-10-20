package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.ImcBaseProfile
import com.example.lifeai_mobile.model.ImcRecordRequest
import com.example.lifeai_mobile.model.ImcRecordResponse
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.PerfilImcBase
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface  AuthApi {
    @POST("registro/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    // ADICIONE ESTE NOVO MÉTODO
    @POST("login/") // Verifique se o endpoint no seu backend Django está correto
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("imc_base_perfil/")
    suspend fun imcBase(@Body request: PerfilImcBase): Response<Unit>

    @GET("imc_base_dashboard/")
    suspend fun getImcBaseDashboard(): Response<List<ImcBaseProfile>>

    @POST("imc/")
    suspend fun createImcRecord(@Body request: ImcRecordRequest): Response<ImcRecordResponse>
}