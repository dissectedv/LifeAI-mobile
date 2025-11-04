package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.utils.SessionManager
import android.util.Log
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val sessionManager: SessionManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking {
            sessionManager.authToken.first()
        }

        val requestBuilder = originalRequest.newBuilder()
        val path = originalRequest.url.encodedPath

        if (token != null && token.isNotBlank() && !path.contains("api/token/refresh") && !path.contains("login")) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}
