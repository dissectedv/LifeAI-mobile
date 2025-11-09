package com.example.lifeai_mobile.viewmodel

import android.app.Application // <-- 1. IMPORTAR
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.utils.SessionManager

class DietaViewModelFactory(
    private val application: Application, // <-- 2. ADICIONAR O 'application'
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DietaViewModel::class.java)) {
            // 3. Passar o 'application' para o ViewModel
            return DietaViewModel(application, repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}