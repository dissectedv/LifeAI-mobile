package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository

class ComposicaoCorporalViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ComposicaoCorporalViewModel::class.java)) {
            return ComposicaoCorporalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}