package com.example.lifeai_mobile.viewmodel // 1. PACOTE CORRIGIDO

import com.example.lifeai_mobile.utils.SessionManager // 2. IMPORT CORRIGIDO
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.viewmodel.OnboardingViewModel

class OnboardingViewModelFactory(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager // Agora sabe o que é SessionManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // PASSE-O PARA O VIEWMODEL
            return OnboardingViewModel(repository, sessionManager) as T // Agora encontra OnboardingViewModel
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}