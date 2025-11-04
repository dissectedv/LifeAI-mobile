package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository

class HistoricoImcViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoricoImcViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoricoImcViewModel(repository) as T // Assumindo que seu VM recebe o repositório
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}