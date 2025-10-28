package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository
import com.example.lifeai_mobile.view.ResumoViewModel

class ResumoViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResumoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ResumoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}