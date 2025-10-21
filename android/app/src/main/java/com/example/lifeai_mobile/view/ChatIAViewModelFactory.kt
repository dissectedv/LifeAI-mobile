package com.example.lifeai_mobile.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lifeai_mobile.viewmodel.ChatIAViewModel

class ChatIAViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatIAViewModel::class.java)) {
            // Se o ViewModel precisasse de um repositório, você o passaria aqui.
            // Ex: return ChatIAViewModel(meuRepositorio) as T
            return ChatIAViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}