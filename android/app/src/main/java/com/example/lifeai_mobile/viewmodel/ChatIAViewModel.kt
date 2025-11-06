package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val DEFAULT_GREETING = "Olá! Eu sou sua assistente virtual LifeAI"

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(DEFAULT_GREETING, isUser = false)
    ),
    val inputText: String = ""
)

data class ChatMessage(val text: String, val isUser: Boolean)

class ChatIAViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val sessaoId: String = UUID.randomUUID().toString()

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

        viewModelScope.launch(Dispatchers.IO) {
            val request = ChatRequest(pergunta = userInput, sessaoId = sessaoId)
            try {
                val response = repository.postChatMessage(request)
                val apiResponse: String = if (response.isSuccessful && response.body() != null) {
                    response.body()?.resposta ?: "A API retornou uma resposta vazia."
                } else {
                    "Erro ao conectar com a IA: ${response.message()}"
                }
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.dropLast(1) + ChatMessage(apiResponse, isUser = false)
                    currentState.copy(messages = updatedMessages)
                }
            } catch (e: Exception) {
                val errorResponse = "Falha na conexão: ${e.message}"
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.dropLast(1) + ChatMessage(errorResponse, isUser = false)
                    currentState.copy(messages = updatedMessages)
                }
            }
        }
    }

    fun setInitialGreeting(greeting: String) {
        if (_uiState.value.messages.size == 1 && _uiState.value.messages.first().text == DEFAULT_GREETING) {
            _uiState.update {
                it.copy(messages = listOf(ChatMessage(greeting, isUser = false)))
            }
        }
    }
}