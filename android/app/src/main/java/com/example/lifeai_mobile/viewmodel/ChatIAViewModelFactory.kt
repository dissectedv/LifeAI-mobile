package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.repository.AuthRepository

class ChatIAViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatIAViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatIAViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}