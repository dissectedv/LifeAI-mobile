package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.model.ChatResponse
import com.example.lifeai_mobile.model.Compromisso
import com.example.lifeai_mobile.model.ComposicaoCorporalRegistro
import com.example.lifeai_mobile.model.ComposicaoCorporalRequest
import com.example.lifeai_mobile.model.DietaResponse
import com.example.lifeai_mobile.model.ExerciseSessionRequest
import com.example.lifeai_mobile.model.ExerciseSessionResponse
import com.example.lifeai_mobile.model.ImcRegistro
import com.example.lifeai_mobile.model.LoginRequest
import com.example.lifeai_mobile.model.LoginResponse
import com.example.lifeai_mobile.model.LogoutRequest
import com.example.lifeai_mobile.model.PerfilRequest
import com.example.lifeai_mobile.model.PerfilResponse
import com.example.lifeai_mobile.model.RegistroImcRequest
import com.example.lifeai_mobile.model.RefreshTokenRequest
import com.example.lifeai_mobile.model.RefreshTokenResponse
import com.example.lifeai_mobile.model.RegisterRequest
import com.example.lifeai_mobile.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("registro/")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("login/")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("perfil/")
    suspend fun createProfile(@Body request: PerfilRequest): Response<Unit>

    @POST("imc/")
    suspend fun createImcRecord(@Body request: RegistroImcRequest): Response<Unit>

    @GET("perfil/")
    suspend fun getProfileData(): Response<PerfilResponse>

    @PATCH("perfil/")
    suspend fun updateProfileData(@Body data: PerfilRequest): Response<PerfilResponse>

    @POST("chat-ia/")
    suspend fun postChatMessage(@Body request: ChatRequest): Response<ChatResponse>

    @POST("gerar-dieta-ia/")
    suspend fun postDietaRequest(@Body request: ChatRequest): Response<DietaResponse>

    @GET("gerar-dieta-ia/")
    suspend fun getDietaAtual(): Response<DietaResponse>

    @GET("imc/registrosConsultas/")
    suspend fun getHistoricoImc(): Response<List<ImcRegistro>>

    @DELETE("imc/{id}/")
    suspend fun deleteImcRegistro(@Path("id") id: Int): Response<Unit>

    @POST("api/token/refresh/")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @POST("logout/")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>

    @POST("send-email/")
    suspend fun sendEmail(@Body emailData: Map<String, String>): Response<Unit>

    @GET("compromissos/")
    suspend fun getCompromissos(): Response<List<Compromisso>>

    @POST("compromissos/")
    suspend fun createCompromisso(@Body compromisso: Compromisso): Response<Compromisso>

    @PATCH("compromissos/{id}/")
    suspend fun updateCompromisso(@Path("id") id: Int, @Body compromisso: Compromisso): Response<Compromisso>

    @DELETE("compromissos/{id}/")
    suspend fun deleteCompromisso(@Path("id") id: Int): Response<Unit>

    @GET("composicao-corporal/")
    suspend fun getHistoricoComposicao(): Response<List<ComposicaoCorporalRegistro>>

    @POST("composicao-corporal/")
    suspend fun createComposicaoRecord(@Body request: ComposicaoCorporalRequest): Response<ComposicaoCorporalRegistro>

    @POST("exercicios/")
    suspend fun saveExerciseSession(@Body request: ExerciseSessionRequest): Response<Unit>

    @GET("exercicios/")
    suspend fun getExerciseHistory(): Response<List<ExerciseSessionResponse>>
}