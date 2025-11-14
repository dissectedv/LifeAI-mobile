package com.example.lifeai_mobile.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifeai_mobile.model.ChatRequest
import com.example.lifeai_mobile.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

private const val DEFAULT_GREETING_TEXT = "Olá! Eu sou sua assistente virtual LifeAI"
private const val LOADING_TEXT = "..."

data class ChatUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(DEFAULT_GREETING_TEXT, isUser = false)
    ),
    val inputText: String = "",
    val isSendingMessage: Boolean = false
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false
)

class ChatIAViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private val sessaoId: String = UUID.randomUUID().toString()
    private var chatJob: Job? = null

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        if (_uiState.value.isSendingMessage) return
        val userInput = _uiState.value.inputText.trim()
        if (userInput.isBlank()) return
        _uiState.update { it.copy(inputText = "") }
        addMessageAndProcessResponse(userInput)
    }

    fun sendSuggestion(suggestion: String) {
        if (_uiState.value.isSendingMessage) return
        val userInput = suggestion.trim()
        if (userInput.isBlank()) return
        addMessageAndProcessResponse(userInput)
    }

    private fun addMessageAndProcessResponse(userInput: String) {
        chatJob?.cancel()

        val userMessage = ChatMessage(userInput, isUser = true)
        val thinkingMessage = ChatMessage(LOADING_TEXT, isUser = false, isLoading = true)

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage + thinkingMessage,
                isSendingMessage = true
            )
        }

        chatJob = viewModelScope.launch(Dispatchers.IO) {
            val startTime = System.currentTimeMillis()
            val request = ChatRequest(pergunta = userInput, sessaoId = sessaoId)
            try {
                val response = repository.postChatMessage(request)
                val apiResponse: String = if (response.isSuccessful && response.body() != null) {
                    response.body()?.resposta ?: "A API retornou uma resposta vazia."
                } else {
                    "Erro ao conectar com a IA: ${response.message()}"
                }

                val duration = System.currentTimeMillis() - startTime
                val minWait = 1500L
                if (duration < minWait) {
                    delay(minWait - duration)
                }

                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.dropLast(1) + ChatMessage(apiResponse, isUser = false)
                    currentState.copy(
                        messages = updatedMessages,
                        isSendingMessage = false
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) {
                    _uiState.update { currentState ->
                        val updatedMessages = currentState.messages.dropLast(1)
                        currentState.copy(
                            messages = updatedMessages,
                            isSendingMessage = false
                        )
                    }
                    return@launch
                }
                val errorResponse = "Falha na conexão: ${e.message}"
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.dropLast(1) + ChatMessage(errorResponse, isUser = false)
                    currentState.copy(
                        messages = updatedMessages,
                        isSendingMessage = false
                    )
                }
            }
        }
    }

    fun stopMessageGeneration() {
        chatJob?.cancel()
    }

    fun setInitialGreeting(greeting: String) {
        if (_uiState.value.messages.size == 1 && _uiState.value.messages.first().text == DEFAULT_GREETING_TEXT) {
            _uiState.update {
                it.copy(messages = listOf(ChatMessage(greeting, isUser = false)))
            }
        }
    }
}