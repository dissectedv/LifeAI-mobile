package com.example.lifeai_mobile.model

import SessionManager
import com.example.lifeai_mobile.repository.AuthApi
import com.example.lifeai_mobile.repository.AuthInterceptor
import com.example.lifeai_mobile.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor // <-- Importe o inspetor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance(sessionManager: SessionManager) {

    // Adicionado o inspetor de log aqui
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Nível BODY para ver tudo
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionManager)) // Primeiro, seu interceptor de autenticação
        .addInterceptor(loggingInterceptor) // Depois, o interceptor de log
        .build()

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // O cliente já contém os dois interceptors
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}