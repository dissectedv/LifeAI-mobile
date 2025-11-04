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

class RetrofitInstance(sessionManager: SessionManager) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authenticator = TokenRefreshAuthenticator(sessionManager)

    private val client = OkHttpClient.Builder()
        .authenticator(authenticator)
        .addInterceptor(AuthInterceptor(sessionManager))
        .addInterceptor(loggingInterceptor)
        .build()

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}