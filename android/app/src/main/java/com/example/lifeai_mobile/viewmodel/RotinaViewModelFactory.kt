package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository

class RotinaViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RotinaViewModel::class.java)) {
            return RotinaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}