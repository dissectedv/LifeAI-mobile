package com.example.lifeai_mobile

import android.app.Application
import android.app.NotificationChannel // <-- Import
import android.app.NotificationManager // <-- Import
import android.content.Context // <-- Import
import android.os.Build // <-- Import
import android.util.Log
import com.example.lifeai_mobile.model.RetrofitInstance
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager

class MyApplication : Application() {

    lateinit var sessionManager: SessionManager
    lateinit var retrofitInstance: RetrofitInstance
    lateinit var authRepository: AuthRepository

    // 1. Companion object para guardar o ID do canal
    companion object {
        const val DIETA_CHANNEL_ID = "dieta_channel"
    }

    override fun onCreate() {
        super.onCreate()

        sessionManager = SessionManager(applicationContext)
        retrofitInstance = RetrofitInstance(sessionManager)
        authRepository = AuthRepository(retrofitInstance.api, sessionManager)

        Log.d("INSTANCE_DEBUG", "MyApplication criou SessionManager: $sessionManager")

        // 2. Chamada para criar o canal
        createNotificationChannels()
    }

    // 3. Função privada para criar o canal (só roda em Android 8+)
    private fun createNotificationChannels() {
        // A verificação de versão é OBRIGATÓRIA.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DIETA_CHANNEL_ID,
                "Notificações de Dieta", // Nome que o usuário vê
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para quando sua dieta gerada pela IA estiver pronta."
            }

            // Registrar o canal no sistema
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}