package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Olá! Eu sou sua assistente virtual LifeAI 🤖", isUser = false)
    ),
    val inputText: String = ""
)

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatIAViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val userInput = _uiState.value.inputText.trim()
        if (userInput.isBlank()) return

        addMessageAndProcessResponse(userInput)

        _uiState.update { it.copy(inputText = "") }
    }

    fun sendSuggestion(suggestion: String) {
        val userInput = suggestion.trim()
        if (userInput.isBlank()) return

        addMessageAndProcessResponse(userInput)
    }

    private fun addMessageAndProcessResponse(userInput: String) {
        val userMessage = ChatMessage(userInput, isUser = true)
        val thinkingMessage = ChatMessage("Processando resposta...", isUser = false)

        _uiState.update {
            it.copy(messages = it.messages + userMessage + thinkingMessage)
        }

        viewModelScope.launch {
            delay(2000)
            val apiResponse = "Esta é uma resposta simulada para '$userInput'! 🧠"

            _uiState.update { currentState ->
                val updatedMessages = currentState.messages.dropLast(1) + ChatMessage(apiResponse, isUser = false)
                currentState.copy(messages = updatedMessages)
            }
        }
    }
}