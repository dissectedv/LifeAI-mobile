package com.example.lifeai_mobile.model

import SessionManager
import com.example.lifeai_mobile.repository.AuthApi
import com.example.lifeai_mobile.repository.AuthInterceptor
import com.example.lifeai_mobile.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitInstance(sessionManager: SessionManager) {

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(sessionManager)) // Usa o interceptor
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