package com.example.lifeai_mobile.model

import com.example.lifeai_mobile.utils.SessionManager
import com.example.lifeai_mobile.repository.AuthApi
import com.example.lifeai_mobile.repository.AuthInterceptor
import com.example.lifeai_mobile.repository.TokenRefreshAuthenticator
import com.example.lifeai_mobile.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitInstance(sessionManager: SessionManager) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authenticator = TokenRefreshAuthenticator(sessionManager)

    private val client = OkHttpClient.Builder()
        .authenticator(authenticator)
        .addInterceptor(AuthInterceptor(sessionManager))
        .addInterceptor(loggingInterceptor)
        // --- CORREÇÃO AQUI ---
        // Aumentamos os tempos de espera para 2 minutos
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        // --- FIM DA CORREÇÃO ---
        .build()

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // --- CORREÇÃO FINAL DE SINTAXE ---
            .create(AuthApi::class.java) // <-- Adicionado os dois pontos ::
        // --- FIM DA CORREÇÃO ---
    }
}