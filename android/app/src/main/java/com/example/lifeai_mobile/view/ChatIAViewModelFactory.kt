package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChatIAViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatIAViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatIAViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
