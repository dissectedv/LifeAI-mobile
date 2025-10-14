package com.example.lifeai_mobile

import SessionManager
import android.app.Application
import android.util.Log
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository

class MyApplication : Application() {

    lateinit var sessionManager: SessionManager
    lateinit var retrofitInstance: RetrofitInstance
    lateinit var authRepository: AuthRepository

    override fun onCreate() {
        super.onCreate()

        // A ordem aqui é importante!
        sessionManager = SessionManager(applicationContext)
        retrofitInstance = RetrofitInstance(sessionManager)
        authRepository = AuthRepository(retrofitInstance.api)

        Log.d("INSTANCE_DEBUG", "MyApplication criou SessionManager: $sessionManager")
    }
}