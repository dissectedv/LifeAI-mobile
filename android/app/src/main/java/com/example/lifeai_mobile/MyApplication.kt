package com.example.lifeai_mobile

import com.example.lifeai_mobile.utils.SessionManager
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

        sessionManager = SessionManager(applicationContext)
        retrofitInstance = RetrofitInstance(sessionManager)
        authRepository = AuthRepository(retrofitInstance.api, sessionManager)

        Log.d("INSTANCE_DEBUG", "MyApplication criou SessionManager: $sessionManager")
    }
}
