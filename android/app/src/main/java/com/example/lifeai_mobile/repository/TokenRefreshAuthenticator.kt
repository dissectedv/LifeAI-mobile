package com.example.lifeai_mobile.repository

import com.example.lifeai_mobile.utils.Constants.BASE_URL
import com.example.lifeai_mobile.utils.SessionManager
import com.example.lifeai_mobile.model.RefreshTokenRequest
import com.example.lifeai_mobile.model.RefreshTokenResponse
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class TokenRefreshAuthenticator(
    private val sessionManager: SessionManager
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        Log.d("AUTH_DEBUG", "Authenticator ativado. Resposta ${response.code}.")

        val path = response.request.url.encodedPath
        if (path.contains("/login") || path.contains("/registro") || path.contains("/api/token/refresh")) {
            Log.d("AUTH_DEBUG", "Falha 401 em rota de autenticação ($path). O Authenticator não vai agir.")
            return null
        }

        val currentRefreshToken = runBlocking {
            sessionManager.refreshToken.first()
        }

        if (currentRefreshToken == null) {
            Log.d("AUTH_DEBUG", "Não há refresh token, deslogando.")
            runBlocking { sessionManager.clearTokens() }
            return null
        }

        synchronized(this) {
            val newAccessToken = runBlocking {
                val tokenFromStorage = sessionManager.authToken.first()
                if (response.request.header("Authorization") != "Bearer $tokenFromStorage") {
                    Log.d("AUTH_DEBUG", "Token já atualizado por outra chamada.")
                    tokenFromStorage
                } else {
                    Log.d("AUTH_DEBUG", "Tentando atualizar o token com a API.")
                    getNewToken(currentRefreshToken)
                }
            }

            if (newAccessToken == null) {
                Log.d("AUTH_DEBUG", "Falha ao pegar novo token. Deslogando.")
                runBlocking { sessionManager.clearTokens() }
                return null
            }

            Log.d("AUTH_DEBUG", "Token atualizado com sucesso. Retentando requisição.")
            return response.request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()
        }
    }

    private suspend fun getNewToken(refreshToken: String): String? {
        val client = OkHttpClient()
        val gson = Gson()

        val requestBody = gson.toJson(RefreshTokenRequest(refreshToken))
            .toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(BASE_URL + "api/token/refresh/")
            .post(requestBody)
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val newTokens = gson.fromJson(responseBody, RefreshTokenResponse::class.java)
                sessionManager.saveTokens(newTokens.access, refreshToken)
                newTokens.access
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("AUTH_DEBUG", "Falha na chamada de refresh: ${e.message}")
            null
        }
    }
}