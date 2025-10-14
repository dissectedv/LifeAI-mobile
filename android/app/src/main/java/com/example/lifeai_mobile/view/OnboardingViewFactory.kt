// Em view/OnboardingViewModelFactory.kt
package com.example.lifeai_mobile.view

import SessionManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository

class OnboardingViewModelFactory(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager // <-- RECEBA O SESSION MANAGER
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // PASSE-O PARA O VIEWMODEL
            return OnboardingViewModel(repository, sessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}