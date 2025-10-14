package com.example.lifeai_mobile.repository

import SessionManager
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.d("INSTANCE_DEBUG", "AuthInterceptor está usando SessionManager: $sessionManager")

        val token = runBlocking {
            sessionManager.authToken.first()
        }
        // ESTA É A LINHA MAIS IMPORTANTE PARA O NOSSO DIAGNÓSTICO
        Log.d("AUTH_DEBUG", "Interceptor está enviando requisição. Token lido: $token")

        val request = chain.request().newBuilder()
        if (token != null && token.isNotBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}