package com.example.lifeai_mobile.model

import com.example.lifeai_mobile.repository.AuthApi
import com.example.lifeai_mobile.repository.AuthInterceptor
import com.example.lifeai_mobile.utils.Constants.BASE_URL
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor { token })
        .build()

    val api: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Django container -> app Android
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }
}